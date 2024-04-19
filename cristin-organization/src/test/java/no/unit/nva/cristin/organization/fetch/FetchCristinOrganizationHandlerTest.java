package no.unit.nva.cristin.organization.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.cristin.organization.common.client.CristinOrganizationApiClient;
import no.unit.nva.cristin.organization.common.client.v20230526.FetchCristinOrgClient20230526;
import no.unit.nva.cristin.organization.dto.SubSubUnitDto;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.model.Organization;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.MediaTypes;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.attempt.Try;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Map;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.client.ClientProvider.VERSION_2023_05_26;
import static no.unit.nva.cristin.model.Constants.NONE;
import static no.unit.nva.cristin.model.Constants.NOT_FOUND_MESSAGE_TEMPLATE;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.utils.UriUtils.getCristinUri;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static no.unit.nva.utils.VersioningUtils.ACCEPT_HEADER_KEY_NAME;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FetchCristinOrganizationHandlerTest {

    public static final ObjectMapper restApiMapper = JsonUtils.dtoObjectMapper;
    public static final String IDENTIFIER = "identifier";
    public static final String NON_EXISTING_IDENTIFIER = "1.0.0.0";
    public static final String LIST_OF_UNITS_JSON_FILE = "list_of_unit_185_53_18_14.json";
    public static final String CRISTIN_UNITS_URI = "https://api.cristin-test.uio.no/v2/units?id=185.53.18.14";
    public static final String ENGLISH_LANGUAGE_KEY = "en";
    public static final String DEPARTMENT_OF_MEDICAL_BIOCHEMISTRY = "Department of Medical Biochemistry";
    public static final String EMPTY_JSON = "{}";
    public static final String ACCEPT_HEADER_EXAMPLE = "application/json; version=%s";
    public static final String SOME_IDENTIFIER = "1.2.3.4";
    public static final String CRISTIN_GET_RESPONSE_JSON = "cristinGetResponse.json";
    public static final String CRISTIN_GET_RESPONSE_SUB_UNITS_JSON = "cristinGetResponseSubUnits.json";
    public static final String CRISTIN_GET_RESPONSE_SUB_UNITS_SORTED_JSON = "cristinGetResponseSubUnitsSorted.json";
    public static final Map<String, String> QUERY_PARAM_NO_DEPTH = Map.of(DEPTH, NONE);
    public static final String NVA_GET_RESPONSE_20230526_JSON = "nvaGetResponse20230526.json";
    public static final String NVA_GET_RESPONSE_WITH_SUB_UNITS_20230526_JSON =
        "nvaGetResponseWithSubUnits20230526.json";
    public static final String NORWEGIAN_LANGUAGE_KEY_UPPERCASE = "NO";
    public static final String ACRONYM_UIO = "UIO";
    public static final String ACRONYM_KLM_MBK = "KLM-MBK";
    public static final String ERROR_MESSAGE_NOT_FOUND =
        "The requested resource 'https://api.dev.nva.aws.unit.no/cristin/organization/1.0.0.0' was not found";

    private FetchCristinOrganizationHandler fetchCristinOrganizationHandler;
    private CristinOrganizationApiClient cristinApiClient;
    private FetchCristinOrgClient20230526 fetchOrgClient20230526;
    private DefaultOrgFetchClientProvider clientProvider;
    private ByteArrayOutputStream output;
    private Context context;

    @BeforeEach
    void setUp() throws ApiGatewayException {
        context = mock(Context.class);
        var mockHttpClient = mock(HttpClient.class);
        cristinApiClient = new CristinOrganizationApiClient(mockHttpClient);
        cristinApiClient = spy(cristinApiClient);
        doReturn(Try.of(new HttpResponseFaker(EMPTY_JSON)))
            .when(cristinApiClient).sendRequestMultipleTimes(any());
        fetchOrgClient20230526 = new FetchCristinOrgClient20230526(mockHttpClient);
        fetchOrgClient20230526 = spy(fetchOrgClient20230526);
        doReturn(new HttpResponseFaker(EMPTY_JSON))
            .when(fetchOrgClient20230526).fetchGetResult(any());
        clientProvider = new DefaultOrgFetchClientProvider();
        clientProvider = spy(clientProvider);
        doReturn(cristinApiClient).when(clientProvider).getVersionOne();
        doReturn(fetchOrgClient20230526).when(clientProvider).getVersion20230526();
        output = new ByteArrayOutputStream();
        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(clientProvider, new Environment());
    }

    @Test
    void shouldReturnsNotFoundResponseWhenUnitIsMissing() throws IOException {
        doReturn(Try.of(new HttpResponseFaker(EMPTY_JSON, HTTP_NOT_FOUND)))
            .when(cristinApiClient).sendRequestMultipleTimes(any());
        doReturn(cristinApiClient).when(clientProvider).getClient(any());

        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(clientProvider, new Environment());
        fetchCristinOrganizationHandler.handleRequest(generateHandlerRequest(NON_EXISTING_IDENTIFIER), output, context);
        var gatewayResponse = parseFailureResponse();

        assertEquals(HTTP_NOT_FOUND, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getHeaders(), hasKey(CONTENT_TYPE));
        assertThat(gatewayResponse.getHeaders(), hasKey(ACCESS_CONTROL_ALLOW_ORIGIN));

        var actualDetail = getProblemDetail(gatewayResponse);
        var errorMessage = String.format(NOT_FOUND_MESSAGE_TEMPLATE, NON_EXISTING_IDENTIFIER);
        assertThat(actualDetail, containsString(errorMessage));
    }

    @Test
    void shouldReturnsBadRequestResponseOnEmptyInput() throws IOException {
        var inputStream = new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withBody(null)
                .withHeaders(null)
                .withPathParameters(null)
                .build();
        fetchCristinOrganizationHandler.handleRequest(inputStream, output, context);
        var gatewayResponse = parseFailureResponse();
        var actualDetail = gatewayResponse.getBodyObject(Problem.class).getDetail();
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS));
    }

    @Test
    void shouldReturnBadRequestResponseOnIllegalIdentifierInPath() throws IOException {
        var inputStream = new HandlerRequestBuilder<>(restApiMapper)
                .withBody(null)
                .withHeaders(null)
                .withPathParameters(Map.of("identifier", randomString()))
                .build();
        fetchCristinOrganizationHandler.handleRequest(inputStream, output, context);
        var gatewayResponse = parseFailureResponse();
        var actualDetail = gatewayResponse.getBodyObject(Problem.class).getDetail();
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS));
    }

    @Test
    void shouldReturnBadRequestResponseOnMissingPathParam() throws IOException {
        var inputStream = generateHandlerRequestWithMissingPathParameter();
        fetchCristinOrganizationHandler.handleRequest(inputStream, output, context);
        var gatewayResponse = parseFailureResponse();
        var actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS));
    }

    @Test
    void shouldReturnOrganizationHierarchy() throws IOException, ApiGatewayException {
        final var level1 = getCristinUri("185.90.0.0", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_90_0_0.json"))
                .when(cristinApiClient).getSubSubUnitDtoWithMultipleEfforts(level1);
        final var level2a = getCristinUri("185.53.0.0", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_53_0_0.json"))
                .when(cristinApiClient).getSubSubUnitDtoWithMultipleEfforts(level2a);
        final var level2b = getCristinUri("185.50.0.0", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_50_0_0.json"))
                .when(cristinApiClient).getSubSubUnitDtoWithMultipleEfforts(level2b);
        final var level3 = getCristinUri("185.53.18.0", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_53_18_0.json"))
                .when(cristinApiClient).getSubSubUnitDtoWithMultipleEfforts(level3);
        final var level4 = getCristinUri("185.53.18.14", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_53_18_14.json"))
                .when(cristinApiClient).getSubSubUnitDtoWithMultipleEfforts(level4);

        doReturn(cristinApiClient).when(clientProvider).getClient(any());

        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(clientProvider, new Environment());
        final var identifier = "185.53.18.14";
        fetchCristinOrganizationHandler.handleRequest(generateHandlerRequest(identifier), output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output, Organization.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getHeaders(), hasKey(CONTENT_TYPE));
        assertThat(gatewayResponse.getHeaders(), hasKey(ACCESS_CONTROL_ALLOW_ORIGIN));

        var expectedId = getNvaApiId(identifier, ORGANIZATION_PATH);
        var actualOrganization = gatewayResponse.getBodyObject(Organization.class);

        assertEquals(actualOrganization.getId(), expectedId);
        assertThat(actualOrganization.getLabels().get(ENGLISH_LANGUAGE_KEY),
                   containsString(DEPARTMENT_OF_MEDICAL_BIOCHEMISTRY));
        assertThat(actualOrganization.getCountry(), equalTo(NORWEGIAN_LANGUAGE_KEY_UPPERCASE));
        assertThat(actualOrganization.getAcronym(), equalTo(ACRONYM_UIO));
    }


    @Test
    void shouldReturnOrganizationFlatHierarchy() throws IOException {
        var resource = stringFromResources(LIST_OF_UNITS_JSON_FILE);
        var fakeHttpResponse = new HttpResponseFaker(resource, HTTP_OK);
        var cristinUri = URI.create(CRISTIN_UNITS_URI);
        doReturn(Try.of(fakeHttpResponse)).when(cristinApiClient).sendRequestMultipleTimes(cristinUri);
        doReturn(cristinApiClient).when(clientProvider).getClient(any());

        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(clientProvider, new Environment());
        final var identifier = "185.53.18.14";
        fetchCristinOrganizationHandler
                .handleRequest(generateHandlerRequestWithAdditionalQueryParameters(identifier, NONE), output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output,Organization.class);

        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_OK));
        assertThat(gatewayResponse.getHeaders(), hasKey(CONTENT_TYPE));
        assertThat(gatewayResponse.getHeaders(), hasKey(ACCESS_CONTROL_ALLOW_ORIGIN));

        var expectedId = getNvaApiId(identifier, ORGANIZATION_PATH);
        var actualOrganization = gatewayResponse.getBodyObject(Organization.class);

        assertThat(actualOrganization.getId(), equalTo(expectedId));
        assertThat(actualOrganization.getLabels().get(ENGLISH_LANGUAGE_KEY),
                   containsString(DEPARTMENT_OF_MEDICAL_BIOCHEMISTRY));
        assertThat(actualOrganization.getHasPart(), equalTo(null));
        assertThat(actualOrganization.getPartOf(), equalTo(null));
        assertThat(actualOrganization.getCountry(), equalTo(NORWEGIAN_LANGUAGE_KEY_UPPERCASE));
        assertThat(actualOrganization.getAcronym(), equalTo(ACRONYM_KLM_MBK));
    }

    @Test
    void shouldNotCallUpstreamOneMoreTimeForFetchingSubunitsWhenSendingParamDepthWithValueNone() throws Exception {
        var resource = stringFromResources(CRISTIN_GET_RESPONSE_JSON);
        var fakeHttpResponse = new HttpResponseFaker(resource, HTTP_OK);
        doReturn(fakeHttpResponse).when(fetchOrgClient20230526).fetchGetResult(any());

        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(clientProvider, new Environment());
        fetchCristinOrganizationHandler.handleRequest(requestUsingV20230526(QUERY_PARAM_NO_DEPTH), output, context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output, Organization.class);

        verify(fetchOrgClient20230526, times(1)).fetchGetResult(any());
        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_OK));
    }

    @ParameterizedTest(name = "Requesting payload using version 20230526 with depth value {0} should return "
                              + "correct payload")
    @MethodSource("depthAndCorrespondingPayloadArgumentProvider")
    void shouldReturnCorrectPayloadBasedOnQueryParamDepthWhenRequestingVersion20230526(String depth,
                                                                                       String cristinPayload,
                                                                                       String cristinSubsPayload,
                                                                                       String expectedResult)
        throws Exception {

        doReturn(new HttpResponseFaker(cristinPayload)).doReturn(new HttpResponseFaker(cristinSubsPayload))
            .when(fetchOrgClient20230526).fetchGetResult(any());
        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(clientProvider, new Environment());
        Map<String, String> queryParams = nonNull(depth) ? Map.of(DEPTH, depth) : emptyMap();
        var input = requestUsingV20230526(queryParams);
        fetchCristinOrganizationHandler.handleRequest(input, output, context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output, Organization.class);
        var actual = gatewayResponse.getBody();

        JSONAssert.assertEquals(expectedResult, actual, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void shouldReturnVersion20230526AsDefault() throws IOException, ApiGatewayException {
        var resource = stringFromResources(CRISTIN_GET_RESPONSE_JSON);
        var fakeHttpResponse = new HttpResponseFaker(resource, HTTP_OK);
        var cristinSubsPayload = getFromResources(CRISTIN_GET_RESPONSE_SUB_UNITS_JSON);
        var fakeSubUnitResponse = new HttpResponseFaker(cristinSubsPayload);

        doReturn(fakeHttpResponse).doReturn(fakeSubUnitResponse)
            .when(fetchOrgClient20230526).fetchGetResult(any());

        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(clientProvider, new Environment());
        fetchCristinOrganizationHandler.handleRequest(generateHandlerRequest(SOME_IDENTIFIER), output, context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output, Organization.class);

        verify(fetchOrgClient20230526, times(2)).fetchGetResult(any());
        verify(cristinApiClient, times(0)).fetchGetResult(any());
        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_OK));
    }

    @Test
    void shouldReturnsNotFoundResponseWhenUpstreamReturnNotFoundAndVersionIs20230526() throws Exception {
        var fakeHttpResponse = new HttpResponseFaker(EMPTY_JSON, HTTP_NOT_FOUND);
        var fakeSubUnitHttpResponse = new HttpResponseFaker("[]", HTTP_OK);

        doReturn(fakeHttpResponse).doReturn(fakeSubUnitHttpResponse).when(fetchOrgClient20230526).fetchGetResult(any());

        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(clientProvider, new Environment());
        fetchCristinOrganizationHandler.handleRequest(generateHandlerRequest(NON_EXISTING_IDENTIFIER), output, context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);
        var actualDetail = getProblemDetail(gatewayResponse);

        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_NOT_FOUND));
        assertThat(actualDetail, containsString(ERROR_MESSAGE_NOT_FOUND));
    }

    @Test
    void shouldNotDisplayHasPartFieldWhenDepthNotWanted() throws Exception {
        var resource = stringFromResources(CRISTIN_GET_RESPONSE_JSON);
        var fakeHttpResponse = new HttpResponseFaker(resource, HTTP_OK);

        doReturn(fakeHttpResponse).when(fetchOrgClient20230526).fetchGetResult(any());

        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(clientProvider, new Environment());
        fetchCristinOrganizationHandler.handleRequest(generateHandlerRequestWithoutDepth(),
                                                      output,
                                                      context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output, Organization.class);
        var responseBody = gatewayResponse.getBodyObject(Organization.class);

        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_OK));
        assertThat(responseBody.getHasPart(), equalTo(null));
    }

    @RepeatedTest(10)
    void shouldReturnHitsInSortedOrderForVersion20230526() throws Exception {
        doReturn(new HttpResponseFaker(getFromResources(CRISTIN_GET_RESPONSE_JSON)))
            .doReturn(new HttpResponseFaker(getFromResources(CRISTIN_GET_RESPONSE_SUB_UNITS_SORTED_JSON)))
            .when(fetchOrgClient20230526).fetchGetResult(any());
        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(clientProvider, new Environment());
        Map<String, String> queryParams = emptyMap();
        var input = requestUsingV20230526(queryParams);
        fetchCristinOrganizationHandler.handleRequest(input, output, context);

        var gatewayResponse = GatewayResponse.fromOutputStream(output, Organization.class);
        var actual = gatewayResponse.getBodyObject(Organization.class);
        var hasParts = actual.getHasPart();

        assertThat(hasParts.get(0).getId().toString(), containsString("185.15.0.11"));
        assertThat(hasParts.get(1).getId().toString(), containsString("185.15.0.12"));
        assertThat(hasParts.get(2).getId().toString(), containsString("185.15.0.13"));
        assertThat(hasParts.get(3).getId().toString(), containsString("185.15.0.15"));
        assertThat(hasParts.get(4).getId().toString(), containsString("185.15.0.16"));
        assertThat(hasParts.get(5).getId().toString(), containsString("185.15.0.19"));
        assertThat(hasParts.get(6).getId().toString(), containsString("185.15.0.21"));
        assertThat(hasParts.get(7).getId().toString(), containsString("185.15.0.25"));
        assertThat(hasParts.get(8).getId().toString(), containsString("185.15.0.31"));
        assertThat(hasParts.get(9).getId().toString(), containsString("185.15.0.32"));
        assertThat(hasParts.get(10).getId().toString(), containsString("185.15.0.36"));
    }

    private Object getSubSubUnit(String subUnitFile) {
        return SubSubUnitDto.fromJson(IoUtils.stringFromResources(Path.of(subUnitFile)));
    }

    private String stringFromResources(String resourceLocation) {
        return IoUtils.stringFromResources(Path.of(resourceLocation));
    }

    private InputStream generateHandlerRequest(String organizationIdentifier) throws JsonProcessingException {
        var headers = Map.of(CONTENT_TYPE, MediaTypes.APPLICATION_JSON_LD.type());
        var pathParameters = Map.of(IDENTIFIER, organizationIdentifier);
        return new HandlerRequestBuilder<>(restApiMapper)
                .withHeaders(headers)
                .withPathParameters(pathParameters)
                .build();
    }

    @SuppressWarnings("SameParameterValue")
    private InputStream generateHandlerRequestWithAdditionalQueryParameters(String organizationIdentifier, String depth)
            throws JsonProcessingException {
        var headers = Map.of(CONTENT_TYPE, MediaTypes.APPLICATION_JSON_LD.type());
        var pathParameters = Map.of(IDENTIFIER, organizationIdentifier);
        var queryParameters = Map.of(DEPTH, depth);
        return new HandlerRequestBuilder<>(restApiMapper)
                .withHeaders(headers)
                .withPathParameters(pathParameters)
                .withQueryParameters(queryParameters)
                .build();
    }


    private InputStream generateHandlerRequestWithMissingPathParameter() throws JsonProcessingException {
        return new HandlerRequestBuilder<>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, MediaTypes.APPLICATION_JSON_LD.type()))
                .build();
    }

    private String getProblemDetail(GatewayResponse<Problem> gatewayResponse) throws JsonProcessingException {
        return gatewayResponse.getBodyObject(Problem.class).getDetail();
    }

    private GatewayResponse<Problem> parseFailureResponse() throws JsonProcessingException {
        var responseWithProblemType = restApiMapper.getTypeFactory()
                .constructParametricType(GatewayResponse.class, Problem.class);
        return restApiMapper.readValue(output.toString(), responseWithProblemType);
    }

    private InputStream requestUsingV20230526(Map<String, String> queryParameters) throws JsonProcessingException {
        var headers = Map.of(CONTENT_TYPE, MediaTypes.APPLICATION_JSON_LD.type(),
                             ACCEPT_HEADER_KEY_NAME, String.format(ACCEPT_HEADER_EXAMPLE, VERSION_2023_05_26));
        var pathParameters = Map.of(IDENTIFIER, SOME_IDENTIFIER);
        return new HandlerRequestBuilder<>(restApiMapper)
                   .withHeaders(headers)
                   .withPathParameters(pathParameters)
                   .withQueryParameters(queryParameters)
                   .build();
    }

    public static Stream<Arguments> depthAndCorrespondingPayloadArgumentProvider() {
        return Stream.of(
            Arguments.of(
                NONE,
                getFromResources(CRISTIN_GET_RESPONSE_JSON),
                null,
                getFromResources(NVA_GET_RESPONSE_20230526_JSON)
            ),
            Arguments.of(
                null,
                getFromResources(CRISTIN_GET_RESPONSE_JSON),
                getFromResources(CRISTIN_GET_RESPONSE_SUB_UNITS_JSON),
                getFromResources(NVA_GET_RESPONSE_WITH_SUB_UNITS_20230526_JSON)
            )
        );
    }

    private static String getFromResources(String filename) {
        return IoUtils.stringFromResources(Path.of(filename));
    }

    private InputStream generateHandlerRequestWithoutDepth()
        throws JsonProcessingException {

        var headers = Map.of(CONTENT_TYPE, MediaTypes.APPLICATION_JSON_LD.type());
        var pathParameters = Map.of(IDENTIFIER, SOME_IDENTIFIER);
        var queryParams = Map.of(DEPTH, NONE);

        return new HandlerRequestBuilder<>(restApiMapper)
                   .withHeaders(headers)
                   .withPathParameters(pathParameters)
                   .withQueryParameters(queryParams)
                   .build();
    }

}
