package no.unit.nva.cristin.projects.query.organization;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_TEMPLATE_REQUIRED_MISSING;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.Constants.EQUAL_OPERATOR;
import static no.unit.nva.cristin.model.JsonPropertyNames.BIOBANK_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.FUNDING;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_KEYWORD;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_SORT;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_UNIT;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.VALID_QUERY_PARAMETER_NVA_KEYS;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.apigateway.MediaTypes.APPLICATION_JSON_LD;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.problem.Problem;

class QueryCristinOrganizationProjectHandlerTest {

    public static final ObjectMapper restApiMapper = JsonUtils.dtoObjectMapper;
    public static final String DUMMY_ORGANIZATION_IDENTIFIER = "4.3.2.1";
    public static final String SAMPLE_PAGE = "2";
    public static final String SAMPLE_RESULTS_SIZE = "10";
    public static final String INVALID_KEY = "invalid";
    public static final String INVALID_VALUE = "value";
    private static final String EMPTY_LIST_STRING = "[]";
    public static final String START_DATE = "start_date";
    public static final String DUMMY_UNIT_ID = "184.12.60.0";
    public static final String FUNDING_SAMPLE = "NRE:1234";
    public static final String FUNDING_SAMPLE_ENCODED = "NRE%3A1234";
    public static final String BIOBANK_SAMPLE = String.valueOf(randomInteger());
    public static final String KEYWORD_SAMPLE = randomString();
    public static final String CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_RESOURCE = "cristinQueryProjectsResponse.json";
    public static final String CRISTIN_GET_PROJECT_RESPONSE_JSON_RESOURCE = "cristinGetProjectResponse.json";

    private QueryCristinOrganizationProjectHandler handler;
    private ByteArrayOutputStream output;
    private Context context;
    private QueryCristinOrganizationProjectApiClient cristinApiClient;

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        HttpClient mockHttpClient = mock(HttpClient.class);
        cristinApiClient = new QueryCristinOrganizationProjectApiClient(mockHttpClient);
        output = new ByteArrayOutputStream();
        handler = new QueryCristinOrganizationProjectHandler(cristinApiClient, new Environment());
    }

    @Test
    void shouldReturnBadRequestResponseOnMissingPathParam() throws IOException {
        var inputStream = generateHandlerRequestWithoutOrganizationIdentifier();
        handler.handleRequest(inputStream, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);
        var actualDetail = getProblemDetail(gatewayResponse);

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_TEMPLATE_REQUIRED_MISSING.substring(0,34)));
    }

    @Test
    void shouldReturnBadRequestResponseOnInvalidQueryParameters() throws IOException {
        var inputStream = generateHandlerDummyRequestWithIllegalQueryParameters();
        handler.handleRequest(inputStream, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);
        var actualDetail = getProblemDetail(gatewayResponse);

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(validQueryParameterNamesMessage(VALID_QUERY_PARAMETER_NVA_KEYS)));
    }

    @Test
    void shouldReturnResponseWithCorrectNumberOfResultsOnValidInput() throws Exception {
        var cristinListOfProjects = getStringFromResources(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_RESOURCE);
        var cristinProject = getStringFromResources(CRISTIN_GET_PROJECT_RESPONSE_JSON_RESOURCE);
        var fakeHttpQueryResponse = new HttpResponseFaker(cristinListOfProjects, HttpURLConnection.HTTP_OK);
        var fakeHttpEnrichResponse = new HttpResponseFaker(cristinProject, HttpURLConnection.HTTP_OK);

        cristinApiClient = spy(cristinApiClient);
        doReturn(fakeHttpQueryResponse).when(cristinApiClient).listProjects(any());
        doReturn(CompletableFuture.completedFuture(fakeHttpEnrichResponse)).when(cristinApiClient)
            .fetchGetResultAsync(any());
        handler = new QueryCristinOrganizationProjectHandler(cristinApiClient, new Environment());
        handler.handleRequest(generateHandlerDummyRequest(), output, context);

        var response = GatewayResponse.fromOutputStream(output, SearchResponse.class);
        var responseBody = response.getBodyObject(SearchResponse.class);

        assertThat(response.getStatusCode(), equalTo(HTTP_OK));
        assertThat(responseBody.getHits().size(), equalTo(5));
    }

    @Test
    void shouldReturnSearchResponseWithEmptyHitsWhenBackendFetchIsEmpty() throws ApiGatewayException, IOException {

        cristinApiClient = spy(cristinApiClient);
        doReturn(new HttpResponseFaker(EMPTY_LIST_STRING, HttpURLConnection.HTTP_OK)).when(cristinApiClient)
            .listProjects(any());
        doReturn(Collections.emptyList()).when(cristinApiClient).fetchQueryResultsOneByOne(any());
        handler = new QueryCristinOrganizationProjectHandler(cristinApiClient, new Environment());
        handler.handleRequest(generateHandlerDummyRequest(), output, context);
        var response = GatewayResponse.fromOutputStream(output, SearchResponse.class);
        var searchResponse = response.getBodyObject(SearchResponse.class);

        assertThat(searchResponse.getHits().size(), equalTo(0));
    }

    @Test
    void shouldAddParamsToCristinQueryForFilteringAndReturnOk() throws IOException, ApiGatewayException {
        cristinApiClient = spy(cristinApiClient);
        doReturn(new HttpResponseFaker(EMPTY_LIST_STRING, HttpURLConnection.HTTP_OK))
            .when(cristinApiClient).listProjects(any());
        handler = new QueryCristinOrganizationProjectHandler(cristinApiClient, new Environment());
        var queryParams = Map.of("funding", FUNDING_SAMPLE,
                "biobank", BIOBANK_SAMPLE,
                "keyword", KEYWORD_SAMPLE,
                "results", "5",
                "unit", DUMMY_UNIT_ID,
                "sort", START_DATE);
        handler.handleRequest(generateHandlerProRealisticRequest(queryParams), output, context);
        var captor = ArgumentCaptor.forClass(URI.class);

        verify(cristinApiClient).listProjects(captor.capture());
        var actualURI = captor.getValue().toString();
        assertThat(actualURI, containsString("page=5"));
        assertThat(actualURI, containsString(BIOBANK_ID + EQUAL_OPERATOR + BIOBANK_SAMPLE));
        assertThat(actualURI, containsString(FUNDING + EQUAL_OPERATOR + FUNDING_SAMPLE_ENCODED));
        assertThat(actualURI, containsString(PROJECT_KEYWORD + EQUAL_OPERATOR + KEYWORD_SAMPLE));
        assertThat(actualURI, containsString(PROJECT_UNIT + EQUAL_OPERATOR + DUMMY_UNIT_ID));
        assertThat(actualURI, containsString(PROJECT_SORT + EQUAL_OPERATOR + START_DATE));

        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                SearchResponse.class);
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    private String getStringFromResources(String cristinQueryProjectsResponseJsonResource) {
        return IoUtils.stringFromResources(Path.of(cristinQueryProjectsResponseJsonResource));
    }

    private InputStream generateHandlerDummyRequestWithIllegalQueryParameters() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .withPathParameters(Map.of(IDENTIFIER, DUMMY_ORGANIZATION_IDENTIFIER))
                .withQueryParameters(Map.of(INVALID_KEY, INVALID_VALUE))
                .build();
    }

    private InputStream generateHandlerDummyRequest() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .withPathParameters(Map.of(IDENTIFIER, DUMMY_ORGANIZATION_IDENTIFIER))
                .withQueryParameters(Map.of(PAGE, SAMPLE_PAGE))
                .withQueryParameters(Map.of(NUMBER_OF_RESULTS, SAMPLE_RESULTS_SIZE))
                .build();
    }

    private InputStream generateHandlerProRealisticRequest(Map<String, String> queryParametersMap)
            throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .withPathParameters(Map.of(IDENTIFIER, DUMMY_ORGANIZATION_IDENTIFIER))
                .withQueryParameters(queryParametersMap)
                .build();
    }

    private InputStream generateHandlerRequestWithoutOrganizationIdentifier() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .build();
    }

    private String getProblemDetail(GatewayResponse<Problem> gatewayResponse) throws JsonProcessingException {
        return gatewayResponse.getBodyObject(Problem.class).getDetail();
    }

}
