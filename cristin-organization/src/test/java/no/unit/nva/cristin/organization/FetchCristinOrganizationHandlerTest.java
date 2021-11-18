package no.unit.nva.cristin.organization;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.MediaTypes;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.apigateway.ApiGatewayHandler.MESSAGE_FOR_RUNTIME_EXCEPTIONS_HIDING_IMPLEMENTATION_DETAILS_TO_API_CLIENTS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class FetchCristinOrganizationHandlerTest {

    public static final ObjectMapper restApiMapper = JsonUtils.dtoObjectMapper;
    public static final String IDENTIFIER = "identifier";
    public static final String IDENTIFIER_VALUE = "1.0.0.0";
    public static final String ORGANIZATION_NOT_FOUND_MESSAGE = "Organization not found: ";
    FetchCristinOrganizationHandler fetchCristinOrganizationHandler;
    private CristinApiClient cristinApiClient;
    private ByteArrayOutputStream output;
    private Context context;

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        cristinApiClient = new CristinApiClient();
        output = new ByteArrayOutputStream();
        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(cristinApiClient);
    }

    @Test
    void shouldReturnsNotFoundResponseWhenUnitIsMissing()
            throws IOException, ApiGatewayException, InterruptedException {

        cristinApiClient = spy(cristinApiClient);
        doThrow(new NotFoundException("Organization not found: " + IDENTIFIER_VALUE))
                .when(cristinApiClient).getSingleUnit(any());

        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(cristinApiClient);
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
        CristinApiClient serviceThrowingException = spy(cristinApiClient);
        try {
            doThrow(new NullPointerException())
                    .when(serviceThrowingException)
                    .getSingleUnit(any());
        } catch (ApiGatewayException | InterruptedException e) {
            e.printStackTrace();
        }

        fetchCristinOrganizationHandler = new FetchCristinOrganizationHandler(serviceThrowingException);
        fetchCristinOrganizationHandler.handleRequest(generateHandlerRequest(IDENTIFIER_VALUE), output, context);

        GatewayResponse<Problem> gatewayResponse = parseFailureResponse();
        String actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(
                MESSAGE_FOR_RUNTIME_EXCEPTIONS_HIDING_IMPLEMENTATION_DETAILS_TO_API_CLIENTS));
    }

    private InputStream generateHandlerRequest(String organizationIdentifier) throws JsonProcessingException {
        Map<String, String> headers = Map.of(CONTENT_TYPE, MediaTypes.APPLICATION_JSON_LD.type());
        Map<String, String> pathParameters = Map.of(IDENTIFIER, organizationIdentifier);
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(headers)
                .withPathParameters(pathParameters)
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
