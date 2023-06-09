package no.unit.nva.cristin.organization.query;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import java.util.stream.Stream;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.organization.common.client.CristinOrganizationApiClient;
import no.unit.nva.cristin.organization.common.client.version20230526.CristinOrgApiClient20230526;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.model.Organization;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.attempt.Try;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import static no.unit.nva.cristin.common.ErrorMessages.ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_DEPTH_INVALID;
import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessage;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.cristin.organization.common.Constants.VERSION_2023_05_26;
import static no.unit.nva.cristin.organization.common.Constants.VERSION_ONE;
import static no.unit.nva.utils.UriUtils.getCristinUri;
import static no.unit.nva.utils.VersioningUtils.ACCEPT_HEADER_KEY_NAME;
import static nva.commons.apigateway.MediaTypes.APPLICATION_JSON_LD;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class QueryCristinOrganizationHandlerTest {

    public static final ObjectMapper restApiMapper = JsonUtils.dtoObjectMapper;
    private static final String CRISTIN_QUERY_RESPONSE = "cristin_query_response_sample.json";
    public static final String EMPTY_ARRAY = "[]";
    public static final String ACCEPT_HEADER_EXAMPLE = "application/json; version=%s";
    private QueryCristinOrganizationHandler queryCristinOrganizationHandler;
    private DefaultOrgQueryClientProvider clientProvider;
    private ByteArrayOutputStream output;
    private Context context;
    private CristinOrganizationApiClient cristinApiClientVersionOne;
    private CristinOrgApiClient20230526 cristinOrgApiClient20230526;

    @BeforeEach
    void setUp() throws ApiGatewayException {
        context = mock(Context.class);
        var httpClient = mock(HttpClient.class);
        cristinApiClientVersionOne = new CristinOrganizationApiClient(httpClient);
        cristinOrgApiClient20230526 = new CristinOrgApiClient20230526(httpClient);
        clientProvider = new DefaultOrgQueryClientProvider();
        clientProvider = spy(clientProvider);

        cristinApiClientVersionOne = spy(cristinApiClientVersionOne);
        cristinOrgApiClient20230526 = spy(cristinOrgApiClient20230526);
        doReturn(Try.of(new HttpResponseFaker(EMPTY_ARRAY)))
            .when(cristinApiClientVersionOne).sendRequestMultipleTimes(any());
        doReturn(new HttpResponseFaker(EMPTY_ARRAY))
            .when(cristinOrgApiClient20230526).fetchQueryResults(any());
        doReturn(cristinApiClientVersionOne).when(clientProvider).getVersionOne();
        doReturn(cristinOrgApiClient20230526).when(clientProvider).getVersion20230526();

        output = new ByteArrayOutputStream();
        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(clientProvider, new Environment());
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
    void shouldReturnStatusOkWithEmptyResponseOnDefaultMockQuery() throws IOException {
        var inputStream = generateHandlerRequestWithRandomQueryParameter();
        queryCristinOrganizationHandler.handleRequest(inputStream, output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);

        var actual = gatewayResponse.getBodyObject(SearchResponse.class);
        assertEquals(0, actual.getHits().size());
        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(JSON_UTF_8.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldReturnResponseOnQuery() throws Exception {
        cristinApiClientVersionOne = spy(cristinApiClientVersionOne);

        final var first = getCristinUri("185.53.18.14", UNITS_PATH);
        doReturn(getOrganization("org_18_53_18_14.json")).when(cristinApiClientVersionOne).getOrganization(first);
        final var second = getCristinUri("2012.9.20.0", UNITS_PATH);
        doReturn(getOrganization("org_2012_9_20_0.json")).when(cristinApiClientVersionOne).getOrganization(second);

        var fakeQueryResponseResource = IoUtils.stringFromResources(Path.of(CRISTIN_QUERY_RESPONSE));
        var fakeHttpResponse = new HttpResponseFaker(fakeQueryResponseResource, HTTP_OK);
        doReturn(getTry(fakeHttpResponse)).when(cristinApiClientVersionOne).sendRequestMultipleTimes(any());

        doReturn(cristinApiClientVersionOne).when(clientProvider).getClient(any());
        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(clientProvider, new Environment());
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

    @ParameterizedTest(name = "Requesting version {0} should call version one {1} times and version two {2} times")
    @MethodSource("versionArgumentProvider")
    void shouldReturnCorrectVersionWhenRequestingItUsingQueryParam(String requestedVersion,
                                                                   int callsVersionOne,
                                                                   int callsVersionTwo)
        throws Exception {
        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(clientProvider, new Environment());
        var input = generateValidHandlerRequestWithVersionParam(requestedVersion);
        queryCristinOrganizationHandler.handleRequest(input, output, context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);

        verify(clientProvider, times(callsVersionOne)).getVersionOne();
        verify(clientProvider, times(callsVersionTwo)).getVersion20230526();
        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_OK));
    }

    @Test
    void shouldReturnVersionOneAsDefault() throws Exception {
        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(clientProvider, new Environment());
        var input = generateHandlerRequestWithRandomQueryParameter();
        queryCristinOrganizationHandler.handleRequest(input, output, context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);

        verify(clientProvider, times(1)).getVersionOne();
        verify(clientProvider, times(0)).getVersion20230526();
        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_OK));
    }

    private InputStream generateValidHandlerRequestWithVersionParam(String versionParam)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                   .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type(),
                                       ACCEPT_HEADER_KEY_NAME, versionParam))
                   .withQueryParameters(Map.of("query", "Department of Medical Biochemistry",
                                               "depth","full"))
                   .build();
    }

    private static Stream<Arguments> versionArgumentProvider() {
        return Stream.of(
            Arguments.of(String.format(ACCEPT_HEADER_EXAMPLE, VERSION_ONE), 1, 0),
            Arguments.of(String.format(ACCEPT_HEADER_EXAMPLE, VERSION_2023_05_26), 0, 1)
        );
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

    private InputStream generateHandlerRequestWithRandomQueryParameter() throws JsonProcessingException {
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