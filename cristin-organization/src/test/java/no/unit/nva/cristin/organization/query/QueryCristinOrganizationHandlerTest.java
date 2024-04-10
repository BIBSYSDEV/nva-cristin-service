package no.unit.nva.cristin.organization.query;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.organization.common.client.CristinOrganizationApiClient;
import no.unit.nva.cristin.organization.common.client.v20230526.FetchCristinOrgClient20230526;
import no.unit.nva.cristin.organization.common.client.v20230526.QueryCristinOrgClient20230526;
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
import org.mockito.ArgumentCaptor;
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
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.ErrorMessages.ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_DEPTH_INVALID;
import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessage;
import static no.unit.nva.client.ClientProvider.VERSION_2023_05_26;
import static no.unit.nva.client.ClientProvider.VERSION_ONE;
import static no.unit.nva.cristin.model.Constants.FULL;
import static no.unit.nva.cristin.model.Constants.INCLUDE_SUB_UNITS;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.SORT;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
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
    public static final String NVA_QUERY_RESPONSE_20230526_JSON = "nvaQueryResponse20230526.json";
    public static final String CRISTIN_QUERY_RESPONSE_V_2_JSON = "cristinQueryResponse.json";
    public static final String CRISTIN_GET_RESPONSE_JSON = "cristinGetResponse.json";
    public static final String CRISTIN_GET_RESPONSE_SUB_UNITS_JSON = "cristinGetResponseSubUnits.json";
    public static final String MEDICAL_BIOCHEMISTRY = "Department of Medical Biochemistry";
    public static final String EMPTY_OBJECT = "{}";

    private QueryCristinOrganizationHandler queryCristinOrganizationHandler;
    private DefaultOrgQueryClientProvider clientProvider;
    private ByteArrayOutputStream output;
    private Context context;
    private CristinOrganizationApiClient cristinApiClientVersionOne;
    private QueryCristinOrgClient20230526 queryCristinOrgClient20230526;
    private HttpClient httpClient;

    @BeforeEach
    void setUp() throws ApiGatewayException {
        context = mock(Context.class);
        httpClient = mock(HttpClient.class);
        cristinApiClientVersionOne = new CristinOrganizationApiClient(httpClient);
        queryCristinOrgClient20230526 = new QueryCristinOrgClient20230526(httpClient);
        clientProvider = new DefaultOrgQueryClientProvider();
        clientProvider = spy(clientProvider);

        cristinApiClientVersionOne = spy(cristinApiClientVersionOne);
        queryCristinOrgClient20230526 = spy(queryCristinOrgClient20230526);
        doReturn(Try.of(new HttpResponseFaker(EMPTY_ARRAY)))
            .when(cristinApiClientVersionOne).sendRequestMultipleTimes(any());
        doReturn(new HttpResponseFaker(EMPTY_ARRAY))
            .when(queryCristinOrgClient20230526).fetchQueryResults(any());
        doReturn(cristinApiClientVersionOne).when(clientProvider).getVersionOne();
        doReturn(queryCristinOrgClient20230526).when(clientProvider).getVersion20230526();

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
        try (var inputStream = generateValidHandlerRequest()) {
            queryCristinOrganizationHandler.handleRequest(inputStream, output, context);
        }

        var gatewayResponse = GatewayResponse.fromOutputStream(output,SearchResponse.class);
        var actual = gatewayResponse.getBodyObject(SearchResponse.class);

        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(2, actual.getHits().size());
        assertEquals(JSON_UTF_8.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    private InputStream generateValidHandlerRequest() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .withQueryParameters(Map.of(QUERY, "Department of Medical Biochemistry", "depth", "full"))
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
    void shouldReturnVersion20230526AsDefault() throws Exception {
        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(clientProvider, new Environment());
        var input = generateHandlerRequestWithRandomQueryParameter();
        queryCristinOrganizationHandler.handleRequest(input, output, context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);

        verify(clientProvider, times(0)).getVersionOne();
        verify(clientProvider, times(1)).getVersion20230526();
        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_OK));
    }

    @Test
    void shouldReturnCorrectPayloadWhenRequestingVersion20230526() throws Exception {
        var fakeQueryResponseResource = IoUtils.stringFromResources(Path.of(CRISTIN_QUERY_RESPONSE_V_2_JSON));
        doReturn(new HttpResponseFaker(fakeQueryResponseResource))
            .when(queryCristinOrgClient20230526).fetchQueryResults(any());

        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(clientProvider, new Environment());
        var input =
            generateValidHandlerRequestWithVersionParam(String.format(ACCEPT_HEADER_EXAMPLE, VERSION_2023_05_26));
        queryCristinOrganizationHandler.handleRequest(input, output, context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output,
                                                               SearchResponse.class);
        var responseBody = gatewayResponse.getBodyObject(SearchResponse.class);

        var expectedSerialized = IoUtils.stringFromResources(Path.of(NVA_QUERY_RESPONSE_20230526_JSON));
        var expected = OBJECT_MAPPER.readValue(expectedSerialized, SearchResponse.class);

        var actualHits = convertHitsToProperFormat(responseBody);
        var expectedHits = convertHitsToProperFormat(expected);

        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_OK));
        assertThat(actualHits.size(), equalTo(2));
        assertThat(readHitsAsTree(actualHits), equalTo(readHitsAsTree(expectedHits)));
    }

    @Test
    void shouldHaveCorrectCristinUriWithParamsOnVersionOne() throws Exception {
        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(clientProvider, new Environment());
        var input = generateHandlerRequestWithAdditionalQueryParameters(String.format(ACCEPT_HEADER_EXAMPLE,
                                                                                      VERSION_ONE));
        queryCristinOrganizationHandler.handleRequest(input, output, context);

        var captor = ArgumentCaptor.forClass(URI.class);
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        verify(cristinApiClientVersionOne).sendRequestMultipleTimes(captor.capture());
        assertThat(captor.getValue().getQuery(), containsString("sort=country+desc"));
        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_OK));
    }

    @Test
    void shouldHaveCorrectCristinUriWithParamsOnVersionTwo() throws Exception {
        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(clientProvider, new Environment());
        var input = generateHandlerRequestWithAdditionalQueryParameters(String.format(ACCEPT_HEADER_EXAMPLE,
                                                                                      VERSION_2023_05_26));
        queryCristinOrganizationHandler.handleRequest(input, output, context);

        var captor = ArgumentCaptor.forClass(URI.class);
        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);

        verify(queryCristinOrgClient20230526).fetchQueryResults(captor.capture());
        assertThat(captor.getValue().getQuery(), containsString("sort=country+desc"));
        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_OK));
    }

    @Test
    void shouldHaveSubUnitsWhenRequested() throws Exception {
        var fetchClient = mockFetchClient();
        queryCristinOrgClient20230526 = new QueryCristinOrgClient20230526(httpClient, fetchClient);
        queryCristinOrgClient20230526 = spy(queryCristinOrgClient20230526);

        var fakeQueryResponseResource = IoUtils.stringFromResources(Path.of(CRISTIN_QUERY_RESPONSE_V_2_JSON));
        doReturn(new HttpResponseFaker(fakeQueryResponseResource))
            .when(queryCristinOrgClient20230526).fetchQueryResults(any());

        doReturn(queryCristinOrgClient20230526).when(clientProvider).getVersion20230526();

        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(clientProvider, new Environment());
        var input = handlerRequestWantingSubUnits();
        queryCristinOrganizationHandler.handleRequest(input, output, context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);
        var responseBody = gatewayResponse.getBodyObject(SearchResponse.class);
        var actualHits = convertHitsToProperFormat(responseBody);

        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_OK));
        assertThat(actualHits.get(0).getHasPart().size(), equalTo(8));
        assertThat(actualHits.get(1).getHasPart().size(), equalTo(8));
    }

    @Test
    void shouldDropEnrichmentWhenIdentifierNotFoundInUpstream() throws Exception {
        var fetchClient = mockFetchClientWithOneHitMissingInUpstream();
        queryCristinOrgClient20230526 = new QueryCristinOrgClient20230526(httpClient, fetchClient);
        queryCristinOrgClient20230526 = spy(queryCristinOrgClient20230526);

        var fakeQueryResponseResource = IoUtils.stringFromResources(Path.of(CRISTIN_QUERY_RESPONSE_V_2_JSON));
        doReturn(new HttpResponseFaker(fakeQueryResponseResource))
            .when(queryCristinOrgClient20230526).fetchQueryResults(any());

        doReturn(queryCristinOrgClient20230526).when(clientProvider).getVersion20230526();

        queryCristinOrganizationHandler = new QueryCristinOrganizationHandler(clientProvider, new Environment());
        var input = handlerRequestWantingSubUnits();
        queryCristinOrganizationHandler.handleRequest(input, output, context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output, SearchResponse.class);
        var responseBody = gatewayResponse.getBodyObject(SearchResponse.class);
        var actualHits = convertHitsToProperFormat(responseBody);

        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_OK));
        assertThat(actualHits.size(), equalTo(1));
    }

    private List<Organization> convertHitsToProperFormat(SearchResponse<?> searchResponse) {
        return OBJECT_MAPPER.convertValue(searchResponse.getHits(), new TypeReference<>() {});
    }

    private JsonNode readHitsAsTree(List<Organization> hits) throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(hits.toString());
    }

    private InputStream generateValidHandlerRequestWithVersionParam(String versionParam)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                   .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type(),
                                       ACCEPT_HEADER_KEY_NAME, versionParam))
                   .withQueryParameters(Map.of(QUERY, "Department of Medical Biochemistry",
                                               "depth", "full"))
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
                .withQueryParameters(Map.of(QUERY, "strangeQueryWithoutHits"))
                .build();
    }

    private InputStream generateHandlerRequestWithIllegalDepthParameter() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .withQueryParameters(Map.of(QUERY, "Fysikk", DEPTH, "feil"))
                .build();
    }

    private InputStream generateHandlerRequestWithAdditionalQueryParameters(String versionParam)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                   .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type(),
                                       ACCEPT_HEADER_KEY_NAME, versionParam))
                   .withQueryParameters(Map.of(QUERY, "strangeQueryWithoutHits",
                                               SORT, "country desc"))
                   .build();
    }

    private String getProblemDetail(GatewayResponse<Problem> gatewayResponse) throws JsonProcessingException {
        return gatewayResponse.getBodyObject(Problem.class).getDetail();
    }

    private FetchCristinOrgClient20230526 mockFetchClient() throws ApiGatewayException {
        var fetchOrgClient20230526 = new FetchCristinOrgClient20230526(httpClient);
        fetchOrgClient20230526 = spy(fetchOrgClient20230526);
        var fakeGetResponseResource = IoUtils.stringFromResources(Path.of(CRISTIN_GET_RESPONSE_JSON));
        var fakeGetSubsResponseResource = IoUtils.stringFromResources(Path.of(CRISTIN_GET_RESPONSE_SUB_UNITS_JSON));

        doReturn(new HttpResponseFaker(fakeGetResponseResource))
            .doReturn(new HttpResponseFaker(fakeGetSubsResponseResource))
            .doReturn(new HttpResponseFaker(fakeGetResponseResource))
            .doReturn(new HttpResponseFaker(fakeGetSubsResponseResource))
            .when(fetchOrgClient20230526).fetchGetResult(any());

        return fetchOrgClient20230526;
    }

    private InputStream handlerRequestWantingSubUnits() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                   .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type(),
                                       ACCEPT_HEADER_KEY_NAME,
                                       String.format(ACCEPT_HEADER_EXAMPLE, VERSION_2023_05_26)))
                   .withQueryParameters(Map.of(QUERY, MEDICAL_BIOCHEMISTRY,
                                               DEPTH, FULL,
                                               INCLUDE_SUB_UNITS, Boolean.TRUE.toString()))
                   .build();
    }

    private FetchCristinOrgClient20230526 mockFetchClientWithOneHitMissingInUpstream() throws ApiGatewayException {
        var fetchClient = new FetchCristinOrgClient20230526(httpClient);
        fetchClient = spy(fetchClient);

        var fakeGetResponseResource = IoUtils.stringFromResources(Path.of(CRISTIN_GET_RESPONSE_JSON));
        var fakeGetSubsResponseResource = IoUtils.stringFromResources(Path.of(CRISTIN_GET_RESPONSE_SUB_UNITS_JSON));

        doReturn(new HttpResponseFaker(fakeGetResponseResource))
            .doReturn(new HttpResponseFaker(fakeGetSubsResponseResource))
            .doReturn(new HttpResponseFaker(EMPTY_OBJECT, HTTP_NOT_FOUND))
            .doReturn(new HttpResponseFaker(EMPTY_ARRAY, HTTP_NOT_FOUND))
            .when(fetchClient).fetchGetResult(any());

        return fetchClient;
    }

}
