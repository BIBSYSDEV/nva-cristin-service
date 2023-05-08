package no.unit.nva.cristin.organization;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.model.Organization;
import no.unit.nva.testutils.HandlerRequestBuilder;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.attempt.Try;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Map;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Collections.emptyList;
import static no.unit.nva.cristin.common.ErrorMessages.ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_DEPTH_INVALID;
import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessage;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.utils.UriUtils.getCristinUri;
import static nva.commons.apigateway.MediaTypes.APPLICATION_JSON_LD;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class QueryCristinOrganizationHandlerTest {

    public static final ObjectMapper restApiMapper = JsonUtils.dtoObjectMapper;
    private static final String CRISTIN_QUERY_RESPONSE = "cristin_query_response_sample.json";
    private QueryCristinOrganizationHandler queryCristinOrganizationHandler;
    private ByteArrayOutputStream output;
    private Context context;

    @BeforeEach
    void setUp() throws NotFoundException, FailedHttpRequestException {
        context = mock(Context.class);
        var cristinApiClient = mock(CristinOrganizationApiClient.class);
        when(cristinApiClient.executeQuery(any())).thenReturn(emptySearchResponse());
        output = new ByteArrayOutputStream();
        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(cristinApiClient, new Environment());
    }

    private SearchResponse<Organization> emptySearchResponse() {
        return new SearchResponse<Organization>(
                new UriWrapper(HTTPS, DOMAIN_NAME)
                        .addChild(BASE_PATH)
                        .addChild(UriUtils.INSTITUTION)
                        .getUri())
                .withProcessingTime(0L)
                .withSize(0)
                .withHits(emptyList());
    }

    @Test
    void shouldReturnBadRequestResponseOnMissingQueryParam() throws IOException {
        var inputStream = generateHandlerRequestWithMissingQueryParameter();
        queryCristinOrganizationHandler.handleRequest(inputStream, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,Problem.class);
        var actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(invalidQueryParametersMessage(
                QUERY, ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE)));
    }

    @Test
    void shouldReturnEmptyResponseOnStrangeQuery() throws IOException {
        var inputStream = generateHandlerRequestWithStrangeQueryParameter();
        queryCristinOrganizationHandler.handleRequest(inputStream, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        var actual = gatewayResponse.getBodyObject(SearchResponse.class);
        assertEquals(0, actual.getHits().size());
        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(JSON_UTF_8.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldReturnResponseOnQuery() throws Exception {
        var mockHttpClient = mock(HttpClient.class);
        var cristinApiClient = new CristinOrganizationApiClient(mockHttpClient);
        cristinApiClient = spy(cristinApiClient);

        final var first = getCristinUri("185.53.18.14", UNITS_PATH);
        doReturn(getOrganization("org_18_53_18_14.json")).when(cristinApiClient).getOrganization(first);
        final var second = getCristinUri("2012.9.20.0", UNITS_PATH);
        doReturn(getOrganization("org_2012_9_20_0.json")).when(cristinApiClient).getOrganization(second);

        var fakeQueryResponseResource = IoUtils.stringFromResources(Path.of(CRISTIN_QUERY_RESPONSE));
        var fakeHttpResponse = new HttpResponseFaker(fakeQueryResponseResource, HTTP_OK);
        doReturn(getTry(fakeHttpResponse)).when(cristinApiClient).sendRequestMultipleTimes(any());

        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(cristinApiClient, new Environment());
        InputStream inputStream = generateValidHandlerRequest();
        queryCristinOrganizationHandler.handleRequest(inputStream, output, context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);
        var actual = gatewayResponse.getBodyObject(SearchResponse.class);

        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(2, actual.getHits().size());
        assertEquals(JSON_UTF_8.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    private InputStream generateValidHandlerRequest() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .withQueryParameters(Map.of("query", "Department of Medical Biochemistry","depth","full"))
                .build();
    }

    @Test
    void shouldReturnBadRequestOnIllegalDepth() throws IOException {
        var inputStream = generateHandlerRequestWithIllegalDepthParameter();
        queryCristinOrganizationHandler.handleRequest(inputStream, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,Problem.class);
        var actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_DEPTH_INVALID));
    }

    private Try<HttpResponse<String>> getTry(HttpResponse<String> mockHttpResponse) {
        return Try.of(mockHttpResponse);
    }

    private Organization getOrganization(String subUnitFile) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(IoUtils.stringFromResources(Path.of(subUnitFile)), Organization.class);
    }

    private InputStream generateHandlerRequestWithMissingQueryParameter() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .build();
    }

    private InputStream generateHandlerRequestWithStrangeQueryParameter() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .withQueryParameters(Map.of("query", "strangeQueryWithoutHits"))
                .build();
    }

    private InputStream generateHandlerRequestWithIllegalDepthParameter() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .withQueryParameters(Map.of("query", "Fysikk",DEPTH, "feil"))
                .build();
    }

    private String getProblemDetail(GatewayResponse<Problem> gatewayResponse) throws JsonProcessingException {
        return gatewayResponse.getBodyObject(Problem.class).getDetail();
    }

}
