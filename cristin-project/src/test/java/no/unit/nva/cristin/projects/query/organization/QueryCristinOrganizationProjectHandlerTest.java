package no.unit.nva.cristin.projects.query.organization;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.ErrorMessages.*;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PROJECTS_PATH;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.VALID_QUERY_PARAMETER_NVA_KEYS;
import static no.unit.nva.cristin.model.JsonPropertyNames.BIOBANK_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.FUNDING;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_KEYWORD;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_SORT;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_UNIT;
import static no.unit.nva.cristin.testing.HttpResponseFaker.LINK_EXAMPLE_VALUE;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.apigateway.MediaTypes.APPLICATION_JSON_LD;
import static nva.commons.core.paths.UriWrapper.HTTPS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.util.Collections;
import java.util.Map;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.zalando.problem.Problem;

class QueryCristinOrganizationProjectHandlerTest {

    private static final ObjectMapper restApiMapper = JsonUtils.dtoObjectMapper;
    private static final String BIOBANK_SAMPLE = String.valueOf(randomInteger());
    private static final String DUMMY_ORGANIZATION_IDENTIFIER = "4.3.2.1";
    private static final String DUMMY_UNIT_ID = "184.12.60.0";
    private static final String EMPTY_LIST_STRING = "[]";
    private static final String FUNDING_SAMPLE = "NRE:1234";
    private static final String INVALID_KEY = "invalid";
    private static final String INVALID_VALUE = "value";
    private static final String KEYWORD_SAMPLE = randomString();
    private static final String SAMPLE_PAGE = "2";
    private static final String SAMPLE_RESULTS_SIZE = "10";
    private static final String START_DATE = "start_date";
    private static final String ZERO_VALUE = "0";

    private QueryCristinOrganizationProjectHandler handler;
    private ByteArrayOutputStream output;
    private Context context;
    private QueryCristinOrganizationProjectApiClient cristinApiClient;

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        cristinApiClient = new QueryCristinOrganizationProjectApiClient();
        output = new ByteArrayOutputStream();
        handler = new QueryCristinOrganizationProjectHandler(cristinApiClient, new Environment());
    }

    @Test
    void shouldReturnBadRequestResponseOnMissingPathParam() throws IOException {
        InputStream inputStream = generateHandlerRequestWithoutOrganizationIdentifier();
        handler.handleRequest(inputStream, output, context);
        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);
        String actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_TEMPLATE_REQUIRED_MISSING.substring(0,34)));
    }

    @Test
    void shouldReturnBadRequestResponseOnInvalidPathParamValue() throws IOException {
        InputStream inputStream = generateHandlerRequestWithInvalidOrganizationIdentifier();
        handler.handleRequest(inputStream, output, context);
        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);
        String actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS));
    }

    @Test
    void shouldReturnBadRequestResponseOnInvalidQueryParameters() throws IOException {
        InputStream inputStream = generateHandlerDummyRequestWithIllegalQueryParameters();
        handler.handleRequest(inputStream, output, context);
        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);
        String actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(validQueryParameterNamesMessage(VALID_QUERY_PARAMETER_NVA_KEYS)));
    }

    @Test
    void shouldReturnOKAndEmptyResponseOnValidDummyInput() throws IOException {
        InputStream inputStream = generateHandlerDummyRequest();
        handler.handleRequest(inputStream, output, context);
        GatewayResponse<SearchResponse> gatewayResponse =
                GatewayResponse.fromOutputStream(output, SearchResponse.class);
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReturnOKAndIdInResponseOnValidDummyInput() throws IOException {
        var  inputStream = generateHandlerDummyRequest();
        handler.handleRequest(inputStream, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        final var searchResponse = gatewayResponse.getBodyObject(SearchResponse.class);

        var idStart = getServiceUri(DUMMY_ORGANIZATION_IDENTIFIER).toString();
        assertTrue(searchResponse.getId().toString().startsWith(idStart));
    }

    @Test
    void shouldReturnSearchResponseWithEmptyHitsWhenBackendFetchIsEmpty() throws ApiGatewayException, IOException {

        QueryCristinOrganizationProjectApiClient apiClient = spy(cristinApiClient);
        doReturn(
            new HttpResponseFaker(
                EMPTY_LIST_STRING, HttpURLConnection.HTTP_OK, generateHeaders(ZERO_VALUE, LINK_EXAMPLE_VALUE))
        ).when(apiClient).listProjects(any());
        doReturn(Collections.emptyList())
            .when(apiClient).fetchQueryResultsOneByOne(any());

        handler = new QueryCristinOrganizationProjectHandler(apiClient, new Environment());
        handler.handleRequest(generateHandlerDummyRequest(), output, context);
        GatewayResponse<SearchResponse> response = GatewayResponse.fromOutputStream(output, SearchResponse.class);
        SearchResponse<NvaProject> searchResponse = response.getBodyObject(SearchResponse.class);
        assertThat(0, equalTo(searchResponse.getHits().size()));
    }

    @Test
    void shouldAddParamsToCristinQueryForFilteringAndReturnOk() throws IOException, ApiGatewayException {
        cristinApiClient = spy(cristinApiClient);
        doReturn(
            new HttpResponseFaker(
                EMPTY_LIST_STRING, HttpURLConnection.HTTP_OK, generateHeaders(ZERO_VALUE, LINK_EXAMPLE_VALUE))
        ).when(cristinApiClient).listProjects(any());

        handler = new QueryCristinOrganizationProjectHandler(cristinApiClient, new Environment());
        var queryParams =
            Map.of("funding", FUNDING_SAMPLE,
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
        assertThat(actualURI, containsString(BIOBANK_ID + "=" + BIOBANK_SAMPLE));
        assertThat(actualURI, containsString(FUNDING + "=" + FUNDING_SAMPLE));
        assertThat(actualURI, containsString(PROJECT_KEYWORD + "=" + KEYWORD_SAMPLE));
        assertThat(actualURI, containsString(PROJECT_UNIT + "=" + DUMMY_UNIT_ID));
        assertThat(actualURI, containsString(PROJECT_SORT + "=" + START_DATE));

        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
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


    private InputStream generateHandlerRequestWithInvalidOrganizationIdentifier() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
            .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
            .withPathParameters(Map.of(ORGANIZATION_PATH, "1.2.3"))
            .build();
    }

    private String getProblemDetail(GatewayResponse<Problem> gatewayResponse) throws JsonProcessingException {
        return gatewayResponse.getBodyObject(Problem.class).getDetail();
    }

    private java.net.http.HttpHeaders generateHeaders(String totalCount, String link) {
        return java.net.http.HttpHeaders.of(HttpResponseFaker.headerMap(totalCount, link), HttpResponseFaker.filter());
    }

    private URI getServiceUri(String identifier) {
        return new UriWrapper(HTTPS,
                DOMAIN_NAME).addChild(BASE_PATH)
                .addChild(ORGANIZATION_PATH)
                .addChild(identifier)
                .addChild(PROJECTS_PATH)
                .getUri();
    }

}
