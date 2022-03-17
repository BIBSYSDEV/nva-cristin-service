package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.person.handler.ListCristinOrganizationPersonsHandler.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LIST;
import static nva.commons.apigateway.MediaTypes.APPLICATION_JSON_LD;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ListCristinOrganizationPersonsHandlerTest {

    public static final ObjectMapper restApiMapper = JsonUtils.dtoObjectMapper;
    public static final String DUMMY_ORGANIZATION_IDENTIFIER = "4.3.2.1";
    public static final String IDENTIFIER_KEY = "identifier";
    public static final String PAGE_KEY = "page";
    public static final String RESULTS_KEY = "results";
    public static final String SAMPLE_PAGE = "2";
    public static final String SAMPLE_RESULTS_SIZE = "10";
    public static final String INVALID_KEY = "invalid";
    public static final String INVALID_VALUE = "value";
    private ListCristinOrganizationPersonsHandler listCristinOrganizationPersonsHandler;
    private ByteArrayOutputStream output;
    private Context context;

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        ApiClient cristinApiClient = mock(ApiClient.class);
        output = new ByteArrayOutputStream();
        listCristinOrganizationPersonsHandler =
                new ListCristinOrganizationPersonsHandler(cristinApiClient, new Environment());
    }

    @Test
    void shouldReturnBadRequestResponseOnMissingPathParam() throws IOException {
        InputStream inputStream = generateHandlerRequestWithoutOrganizationIdentifier();
        listCristinOrganizationPersonsHandler.handleRequest(inputStream, output, context);
        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        String actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS));
    }

    @Test
    void shouldReturnBadRequestResponseOnInvalidQueryParameters() throws IOException {
        InputStream inputStream = generateHandlerDummyRequestWithIllegalQueryParameters();
        listCristinOrganizationPersonsHandler.handleRequest(inputStream, output, context);
        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        String actualDetail = getProblemDetail(gatewayResponse);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(actualDetail, containsString(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LIST));
    }

    @Test
    void shouldReturnOKAndEmptyResponseOnValidDummyInput() throws IOException {
        InputStream inputStream = generateHandlerDummyRequest();
        listCristinOrganizationPersonsHandler.handleRequest(inputStream, output, context);
        GatewayResponse<SearchResponse> gatewayResponse = GatewayResponse.fromOutputStream(output);
        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
    }



    private InputStream generateHandlerDummyRequestWithIllegalQueryParameters() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .withPathParameters(Map.of(IDENTIFIER_KEY, DUMMY_ORGANIZATION_IDENTIFIER))
                .withQueryParameters(Map.of(INVALID_KEY, INVALID_VALUE))
                .build();
    }

    private InputStream generateHandlerDummyRequest() throws JsonProcessingException {
        return new HandlerRequestBuilder<InputStream>(restApiMapper)
                .withHeaders(Map.of(CONTENT_TYPE, APPLICATION_JSON_LD.type()))
                .withPathParameters(Map.of(IDENTIFIER_KEY, DUMMY_ORGANIZATION_IDENTIFIER))
                .withQueryParameters(Map.of(PAGE_KEY, SAMPLE_PAGE))
                .withQueryParameters(Map.of(RESULTS_KEY, SAMPLE_RESULTS_SIZE))
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
