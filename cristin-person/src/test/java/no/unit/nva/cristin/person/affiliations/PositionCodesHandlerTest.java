package no.unit.nva.cristin.person.affiliations;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static no.unit.nva.cristin.common.ErrorMessages.ONLY_SUPPORT_BOOLEAN_VALUES;
import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessage;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.person.affiliations.PositionCodesHandler.ACTIVE_STATUS_QUERY_PARAM;
import static no.unit.nva.testutils.RandomDataGenerator.randomBoolean;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;
import no.unit.nva.cristin.common.ErrorMessages;
import no.unit.nva.cristin.person.affiliations.model.CristinPositionCode;
import no.unit.nva.cristin.person.affiliations.model.PositionCode;
import no.unit.nva.cristin.person.affiliations.model.PositionCodes;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

public class PositionCodesHandlerTest {

    private static final String EMPTY_STRING = "";

    private final HttpClient mockHttpClient = mock(HttpClient.class);
    private final Environment environment = new Environment();
    private CristinPositionCodesClient apiClient;
    private Context context;
    private ByteArrayOutputStream output;
    private PositionCodesHandler handler;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(mockHttpClient.<String>send(any(), any())).thenReturn(new HttpResponseFaker(fakeJsonFromUpstream(), 200));
        apiClient = new CristinPositionCodesClient(mockHttpClient);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new PositionCodesHandler(apiClient, environment);
    }

    @Test
    void shouldReturnBadGatewayWhenUpstreamReturnsInvalidDataButStatusOk() throws IOException, InterruptedException {
        when(mockHttpClient.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_STRING, 200));
        apiClient = new CristinPositionCodesClient(mockHttpClient);
        handler = new PositionCodesHandler(apiClient, environment);
        var gatewayResponse = sendQuery(null);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void shouldReturnAllPositionCodesWhenCallingEndpointWithNoParams() throws IOException {
        var actual = sendQuery(null).getBodyObject(PositionCodes.class);
        List<PositionCode> actualHits = OBJECT_MAPPER.convertValue(actual.getPositions(), new TypeReference<>() { });

        assertThat(actualHits.contains(getOneCode().toPositionCode()), equalTo(true));
        assertThat(actualHits.contains(getAnotherCode().toPositionCode()), equalTo(true));
    }

    @Test
    void shouldReturnRequestedStatusPositionCodesOnlyWhenCallingWithActiveQueryParams() throws IOException {
        var expectedPositionStatus = randomBoolean();
        var queryParamMap = Map.of(ACTIVE_STATUS_QUERY_PARAM, String.valueOf(expectedPositionStatus));
        var actual = sendQuery(queryParamMap).getBodyObject(PositionCodes.class);
        List<PositionCode> actualHits = OBJECT_MAPPER.convertValue(actual.getPositions(), new TypeReference<>() {
        });

        var allPositionCodesShouldBeInRequestedStatus =
            isAllPositionCodesShouldBeInRequestedStatus(expectedPositionStatus, actualHits);
        assertTrue(allPositionCodesShouldBeInRequestedStatus, "All position codes should be in requested status");
    }

    @Test
    void shouldReturnBadRequestResponseWhenCallingWithInvalidActiveQueryParams() throws IOException {
        var queryParamMap = Map.of(ACTIVE_STATUS_QUERY_PARAM, randomString());
        var inputStream = requestWithParams(queryParamMap);
        handler.handleRequest(inputStream, output, context);
        var gatewayResponseForInvalidQueryParam = GatewayResponse.fromOutputStream(output,
                                                                                   Problem.class);
        var actualErrorDetailInResponse = getProblemDetail(gatewayResponseForInvalidQueryParam);

        assertEquals(HTTP_BAD_REQUEST, gatewayResponseForInvalidQueryParam.getStatusCode());
        assertThat(actualErrorDetailInResponse, containsString(invalidQueryParametersMessage(
            ACTIVE_STATUS_QUERY_PARAM, ONLY_SUPPORT_BOOLEAN_VALUES)));
    }

    private boolean isAllPositionCodesShouldBeInRequestedStatus(boolean expectedPositionStatus,
                                                                List<PositionCode> actualHits) {
        return actualHits.stream().allMatch(position -> position.enabled() == expectedPositionStatus);
    }

    private GatewayResponse<PositionCodes> sendQuery(Map<String, String> queryParams) throws IOException {
        try (var input = requestWithParams(queryParams)) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, PositionCodes.class);
    }

    private InputStream requestWithParams(Map<String, String> queryParams)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withQueryParameters(queryParams)
            .build();
    }

    private String fakeJsonFromUpstream() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(List.of(getOneCode(), getAnotherCode()));
    }

    private CristinPositionCode getOneCode() {
        var oneCode = new CristinPositionCode();
        oneCode.setCode("123");
        oneCode.setName(Map.of("en", "Consultant"));
        oneCode.setEnabled(true);
        return oneCode;
    }

    private CristinPositionCode getAnotherCode() {
        var anotherCode = new CristinPositionCode();
        anotherCode.setCode("567");
        anotherCode.setName(Map.of("en", "Janitor", "nb", "Vaktmester"));
        anotherCode.setEnabled(false);
        return anotherCode;
    }

    private String getProblemDetail(GatewayResponse<Problem> gatewayResponse) throws JsonProcessingException {
        return gatewayResponse.getBodyObject(Problem.class).getDetail();
    }
}
