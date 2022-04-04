package no.unit.nva.cristin.organization;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.cristin.organization.dto.SubSubUnitDto;
import no.unit.nva.model.Organization;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.MediaTypes;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.model.Constants.NONE;
import static no.unit.nva.cristin.model.Constants.NOT_FOUND_MESSAGE_TEMPLATE;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.utils.UriUtils.getCristinUri;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static nva.commons.apigateway.ApiGatewayHandler.MESSAGE_FOR_RUNTIME_EXCEPTIONS_HIDING_IMPLEMENTATION_DETAILS_TO_API_CLIENTS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class FetchCristinOrganizationHandlerTest {

    public static final ObjectMapper restApiMapper = JsonUtils.dtoObjectMapper;
    public static final String IDENTIFIER = "identifier";
    public static final String IDENTIFIER_VALUE = "1.0.0.0";
    public static final String ORGANIZATION_NOT_FOUND_MESSAGE = "cannot be dereferenced";
    FetchCristinOrganizationHandler fetchCristinOrganizationHandler;
    private CristinOrganizationApiClient cristinApiClient;
    private ByteArrayOutputStream output;
    private Context context;

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        cristinApiClient = new CristinOrganizationApiClient();
        output = new ByteArrayOutputStream();
        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(cristinApiClient, new Environment());
    }

    @Test
    void shouldReturnsNotFoundResponseWhenUnitIsMissing()
            throws IOException, ApiGatewayException {

        cristinApiClient = spy(cristinApiClient);
        doThrow(new NotFoundException(NOT_FOUND_MESSAGE_TEMPLATE + IDENTIFIER_VALUE))
                .when(cristinApiClient).getOrganization(any());

        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(cristinApiClient, new Environment());
        fetchCristinOrganizationHandler.handleRequest(generateHandlerRequest(IDENTIFIER_VALUE), output, context);
        GatewayResponse<Problem> gatewayResponse = parseFailureResponse();

        assertEquals(HTTP_NOT_FOUND, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getHeaders(), hasKey(CONTENT_TYPE));
        assertThat(gatewayResponse.getHeaders(), hasKey(ACCESS_CONTROL_ALLOW_ORIGIN));

        String actualDetail = getProblemDetail(gatewayResponse);
        assertThat(actualDetail, containsString(ORGANIZATION_NOT_FOUND_MESSAGE));
        assertThat(actualDetail, containsString(IDENTIFIER_VALUE));
    }

    @Test
    void shouldReturnsBadRequestResponseOnEmptyInput() throws IOException {
        InputStream inputStream = new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withBody(null)
                .withHeaders(null)
                .withPathParameters(null)
                .build();
        fetchCristinOrganizationHandler.handleRequest(inputStream, output, context);
        GatewayResponse<Problem> gatewayResponse = parseFailureResponse();
        String actualDetail = gatewayResponse.getBodyObject(Problem.class).getDetail();
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS));
    }

    @Test
    void shouldReturnBadRequestResponseOnIllegalIdentifierInPath() throws IOException {
        InputStream inputStream = new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withBody(null)
                .withHeaders(null)
                .withPathParameters(Map.of("identifier", randomString()))
                .build();
        fetchCristinOrganizationHandler.handleRequest(inputStream, output, context);
        GatewayResponse<Problem> gatewayResponse = parseFailureResponse();
        String actualDetail = gatewayResponse.getBodyObject(Problem.class).getDetail();
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS));
    }

    @Test
    void shouldReturnBadRequestResponseOnMissingPathParam() throws IOException {
        InputStream inputStream = generateHandlerRequestWithMissingPathParameter();
        fetchCristinOrganizationHandler.handleRequest(inputStream, output, context);
        GatewayResponse<Problem> gatewayResponse = parseFailureResponse();
        String actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS));
    }

    @Test
    void shouldReturnInternalServerErrorResponseOnUnexpectedException() throws IOException {
        CristinOrganizationApiClient serviceThrowingException = spy(cristinApiClient);
        try {
            doThrow(new NullPointerException())
                    .when(serviceThrowingException)
                    .getOrganization(any());
        } catch (ApiGatewayException e) {
            e.printStackTrace();
        }

        fetchCristinOrganizationHandler =
                new FetchCristinOrganizationHandler(serviceThrowingException, new Environment());
        fetchCristinOrganizationHandler.handleRequest(generateHandlerRequest(IDENTIFIER_VALUE), output, context);

        GatewayResponse<Problem> gatewayResponse = parseFailureResponse();
        String actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(
                MESSAGE_FOR_RUNTIME_EXCEPTIONS_HIDING_IMPLEMENTATION_DETAILS_TO_API_CLIENTS));
    }

    @Test
    void shouldReturnOrganizationHierarchy() throws IOException, ApiGatewayException {

        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.sendAsync(any(), any())).thenThrow(new RuntimeException("This should Not happen!"));
        output = new ByteArrayOutputStream();
        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(cristinApiClient, new Environment());

        CristinOrganizationApiClient mySpy = spy(cristinApiClient);
        final URI level1 = getCristinUri("185.90.0.0", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_90_0_0.json"))
                .when(mySpy).getSubSubUnitDtoWithMultipleEfforts(level1);
        final URI level2a = getCristinUri("185.53.0.0", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_53_0_0.json"))
                .when(mySpy).getSubSubUnitDtoWithMultipleEfforts(level2a);
        final URI level2b = getCristinUri("185.50.0.0", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_50_0_0.json"))
                .when(mySpy).getSubSubUnitDtoWithMultipleEfforts(level2b);
        final URI level3 = getCristinUri("185.53.18.0", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_53_18_0.json"))
                .when(mySpy).getSubSubUnitDtoWithMultipleEfforts(level3);
        final URI level4 = getCristinUri("185.53.18.14", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_53_18_14.json"))
                .when(mySpy).getSubSubUnitDtoWithMultipleEfforts(level4);

        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(cristinApiClient, new Environment());
        final String identifier = "185.53.18.14";
        fetchCristinOrganizationHandler.handleRequest(generateHandlerRequest(identifier), output, context);
        GatewayResponse<Organization> gatewayResponse = GatewayResponse.fromOutputStream(output,Organization.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getHeaders(), hasKey(CONTENT_TYPE));
        assertThat(gatewayResponse.getHeaders(), hasKey(ACCESS_CONTROL_ALLOW_ORIGIN));

        URI expectedId = getNvaApiId(identifier, ORGANIZATION_PATH);
        Organization actualOrganization = gatewayResponse.getBodyObject(Organization.class);
        assertEquals(actualOrganization.getId(), expectedId);
        assertThat(actualOrganization.getName().get("en"), containsString("Department of Medical Biochemistry"));
        System.out.println(actualOrganization);
    }


    @Test
    void shouldReturnHttp404NotFoundWhenNonExistingIdentifier() throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpResponse<Object> httpResponse = mock(HttpResponse.class);
        CompletableFuture<HttpResponse<Object>> notFoundResponse = mock(CompletableFuture.class);
        when(httpResponse.statusCode()).thenReturn(HTTP_NOT_FOUND);
        when(httpResponse.body()).thenReturn("https://example.org/someidentifier");
        when(notFoundResponse.get()).thenReturn(httpResponse);
        when(httpClient.sendAsync(any(), any())).thenReturn(notFoundResponse);
        CristinOrganizationApiClient cristinApiClient = new CristinOrganizationApiClient(httpClient);
        output = new ByteArrayOutputStream();
        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(cristinApiClient, new Environment());

        fetchCristinOrganizationHandler.handleRequest(generateHandlerRequest(IDENTIFIER_VALUE), output, context);

        GatewayResponse<Problem> gatewayResponse = parseFailureResponse();
        assertEquals(HTTP_NOT_FOUND, gatewayResponse.getStatusCode());
    }


    @Test
    void shouldReturnOrganizationFlatHierarchy() throws IOException, ApiGatewayException {

        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.sendAsync(any(), any())).thenThrow(new RuntimeException("This should Not happen!"));
        output = new ByteArrayOutputStream();
        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(cristinApiClient, new Environment());

        CristinOrganizationApiClient mySpy = spy(cristinApiClient);
        final URI level1 = getCristinUri("185.90.0.0", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_90_0_0.json"))
                .when(mySpy).getSubSubUnitDtoWithMultipleEfforts(level1);
        final URI level2a = getCristinUri("185.53.0.0", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_53_0_0.json"))
                .when(mySpy).getSubSubUnitDtoWithMultipleEfforts(level2a);
        final URI level2b = getCristinUri("185.50.0.0", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_50_0_0.json"))
                .when(mySpy).getSubSubUnitDtoWithMultipleEfforts(level2b);
        final URI level3 = getCristinUri("185.53.18.0", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_53_18_0.json"))
                .when(mySpy).getSubSubUnitDtoWithMultipleEfforts(level3);
        final URI level4 = getCristinUri("185.53.18.14", UNITS_PATH);
        doReturn(getSubSubUnit("unit_18_53_18_14.json"))
                .when(mySpy).getSubSubUnitDtoWithMultipleEfforts(level4);

        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(cristinApiClient, new Environment());
        final String identifier = "185.53.18.14";
        fetchCristinOrganizationHandler
                .handleRequest(generateHandlerRequestWithAdditionalQueryParameters(identifier, NONE), output, context);
        GatewayResponse<Organization> gatewayResponse = GatewayResponse.fromOutputStream(output,Organization.class);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getHeaders(), hasKey(CONTENT_TYPE));
        assertThat(gatewayResponse.getHeaders(), hasKey(ACCESS_CONTROL_ALLOW_ORIGIN));

        URI expectedId = getNvaApiId(identifier, ORGANIZATION_PATH);
        Organization actualOrganization = gatewayResponse.getBodyObject(Organization.class);
        assertEquals(actualOrganization.getId(), expectedId);
        assertThat(actualOrganization.getName().get("en"), containsString("Department of Medical Biochemistry"));
        assertEquals(actualOrganization.getHasPart(), null);
        assertEquals(actualOrganization.getPartOf(), null);
        System.out.println(actualOrganization);
    }


    private Object getSubSubUnit(String subUnitFile) {
        return SubSubUnitDto.fromJson(IoUtils.stringFromResources(Path.of(subUnitFile)));
    }

    private InputStream generateHandlerRequest(String organizationIdentifier) throws JsonProcessingException {
        Map<String, String> headers = Map.of(CONTENT_TYPE, MediaTypes.APPLICATION_JSON_LD.type());
        Map<String, String> pathParameters = Map.of(IDENTIFIER, organizationIdentifier);
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(headers)
                .withPathParameters(pathParameters)
                .build();
    }

    private InputStream generateHandlerRequestWithAdditionalQueryParameters(String organizationIdentifier, String depth)
            throws JsonProcessingException {
        Map<String, String> headers = Map.of(CONTENT_TYPE, MediaTypes.APPLICATION_JSON_LD.type());
        Map<String, String> pathParameters = Map.of(IDENTIFIER, organizationIdentifier);
        Map<String, String> queryParameters = Map.of(DEPTH, depth);
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(headers)
                .withPathParameters(pathParameters)
                .withQueryParameters(queryParameters)
                .build();
    }


    private InputStream generateHandlerRequestWithMissingPathParameter() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, MediaTypes.APPLICATION_JSON_LD.type()))
                .build();
    }

    private String getProblemDetail(GatewayResponse<Problem> gatewayResponse) throws JsonProcessingException {
        return gatewayResponse.getBodyObject(Problem.class).getDetail();
    }

    private GatewayResponse<Problem> parseFailureResponse() throws JsonProcessingException {
        JavaType responseWithProblemType = restApiMapper.getTypeFactory()
                .constructParametricType(GatewayResponse.class, Problem.class);
        return restApiMapper.readValue(output.toString(), responseWithProblemType);
    }
}
