package no.unit.nva.cristin.person.employment.update;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD;
import static no.unit.nva.cristin.common.ErrorMessages.invalidFieldParameterMessage;
import static no.unit.nva.cristin.common.client.PatchApiClient.EMPTY_JSON;
import static no.unit.nva.cristin.model.Constants.EMPLOYMENT_ID;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.FULL_TIME_PERCENTAGE;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_USERS;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Map;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UpdatePersonEmploymentHandlerTest {

    private static final Map<String, String> validPath =
        Map.of(PERSON_ID, randomIntegerAsString(), EMPLOYMENT_ID, randomIntegerAsString());
    private static final String validJson =
        IoUtils.stringFromResources(Path.of("nvaApiCreateEmploymentRequest.json"));
    private static final String INVALID_URI = "https://example.org/hello";
    private static final String INVALID_VALUE = "hello";
    private static final URI TOP_ORG_ID = UriWrapper.fromUri(randomUri()).addChild("185.90.0.0").getUri();

    private final HttpClient httpClientMock = mock(HttpClient.class);
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private UpdatePersonEmploymentHandler handler;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 204));
        UpdatePersonEmploymentClient apiClient = new UpdatePersonEmploymentClient(httpClientMock);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new UpdatePersonEmploymentHandler(apiClient, environment);
    }

    @Test
    void shouldReturnNoContentResponseWhenPayloadIsValid() throws IOException {
        GatewayResponse<Void> gatewayResponse = sendQuery(validJson);

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenClientIsNotAuthenticated() throws IOException {
        GatewayResponse<Void> gatewayResponse = queryWithoutRequiredAccessRights();

        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldThrowBadRequestWhenInvalidPositionCode() throws IOException {
        ObjectNode input = OBJECT_MAPPER.createObjectNode();
        input.put(TYPE, INVALID_URI);
        GatewayResponse<Void> gatewayResponse = sendQuery(input.toString());

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(invalidFieldParameterMessage(TYPE)));
    }

    @Test
    void shouldThrowBadRequestWhenInvalidAffiliation() throws IOException {
        ObjectNode input = OBJECT_MAPPER.createObjectNode();
        input.put(ORGANIZATION, INVALID_URI);
        GatewayResponse<Void> gatewayResponse = sendQuery(input.toString());

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(invalidFieldParameterMessage(ORGANIZATION)));
    }

    @Test
    void shouldThrowBadRequestWhenInvalidDate() throws IOException {
        ObjectNode input = OBJECT_MAPPER.createObjectNode();
        input.put(START_DATE, INVALID_VALUE);
        GatewayResponse<Void> gatewayResponse = sendQuery(input.toString());

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(invalidFieldParameterMessage(START_DATE)));
    }

    @Test
    void shouldThrowBadRequestWhenNonNullableIsNull() throws IOException {
        ObjectNode input = OBJECT_MAPPER.createObjectNode();
        input.putNull(START_DATE);
        GatewayResponse<Void> gatewayResponse = sendQuery(input.toString());

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(invalidFieldParameterMessage(START_DATE)));
    }

    @Test
    void shouldThrowBadRequestWhenInvalidFullTimePercentage() throws IOException {
        ObjectNode input = OBJECT_MAPPER.createObjectNode();
        input.put(FULL_TIME_PERCENTAGE, INVALID_VALUE);
        GatewayResponse<Void> gatewayResponse = sendQuery(input.toString());

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(invalidFieldParameterMessage(FULL_TIME_PERCENTAGE)));
    }

    @Test
    void shouldThrowBadRequestWhenNoSupportedValuesPresentInJson() throws IOException {
        ObjectNode input = OBJECT_MAPPER.createObjectNode();
        input.put(INVALID_VALUE, INVALID_VALUE);
        GatewayResponse<Void> gatewayResponse = sendQuery(input.toString());

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD));
    }

    private static String randomIntegerAsString() {
        return String.valueOf(randomInteger());
    }

    private GatewayResponse<Void> sendQuery(String body) throws IOException {
        try (var input = createRequest(body)) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, Void.class);
    }

    private InputStream createRequest(String body) throws JsonProcessingException {
        var customerId = randomUri();

        return new HandlerRequestBuilder<String>(OBJECT_MAPPER)
                   .withBody(body)
                   .withCurrentCustomer(customerId)
                   .withTopLevelCristinOrgId(TOP_ORG_ID)
                   .withAccessRights(customerId, EDIT_OWN_INSTITUTION_USERS)
                   .withPathParameters(validPath)
                   .build();
    }

    private GatewayResponse<Void> queryWithoutRequiredAccessRights() throws IOException {
        try (var input = new HandlerRequestBuilder<String>(OBJECT_MAPPER)
                             .withBody(EMPTY_JSON)
                             .withPathParameters(validPath)
                             .build()) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, Void.class);
    }
}
