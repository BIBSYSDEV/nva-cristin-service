package no.unit.nva.cristin.person.update;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.common.client.PatchApiClient.EMPTY_JSON;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_FIRST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.FIELD_CAN_NOT_BE_ERASED;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.ORCID_IS_NOT_VALID;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.RESERVED_FIELD_CAN_ONLY_BE_SET_TO_TRUE;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_USERS;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Map;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UpdateCristinPersonHandlerTest {

    private static final Map<String, String> validPath = Map.of(PERSON_ID, randomIntegerAsString());
    private static final String VALID_ORCID = "1234-1234-1234-1234";
    private static final String INVALID_ORCID = "1234";
    private static final String SOME_TEXT = "Hello";

    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private UpdateCristinPersonHandler handler;

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new UpdateCristinPersonHandler(environment);
    }

    @Test
    void shouldReturnNoContentResponseWhenCallingHandlerWithValidJson() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ORCID, VALID_ORCID);
        jsonObject.put(FIRST_NAME, randomString());
        jsonObject.put(PREFERRED_FIRST_NAME, randomString());
        jsonObject.put(PREFERRED_LAST_NAME, JSONObject.NULL);

        GatewayResponse<String> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
        assertEquals(EMPTY_JSON, gatewayResponse.getBodyObject(String.class));
    }

    @Test
    void shouldThrowForbiddenExceptionWhenClientIsNotAuthenticated() throws IOException {
        GatewayResponse<String> gatewayResponse = queryWithoutRequiredAccessRights(EMPTY_JSON);

        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldReturnBadRequestWhenSendingNullBody() throws IOException {
        GatewayResponse<String> gatewayResponse = sendQuery(validPath, null);

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_PAYLOAD));
    }

    @Test
    void shouldReturnOkNoContentWhenOrcidIsPresentAndNull() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ORCID, JSONObject.NULL);

        GatewayResponse<String> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenOrcidHasInvalidValue() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ORCID, INVALID_ORCID);

        GatewayResponse<String> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ORCID_IS_NOT_VALID));
    }

    @Test
    void shouldReturnBadRequestWhenPrimaryNameIsNull() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(FIRST_NAME, JSONObject.NULL);

        GatewayResponse<String> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(String.format(FIELD_CAN_NOT_BE_ERASED, FIRST_NAME)));
    }

    @Test
    void shouldReturnBadRequestWhenReservedIsNull() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(RESERVED, JSONObject.NULL);

        GatewayResponse<String> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(RESERVED_FIELD_CAN_ONLY_BE_SET_TO_TRUE));
    }

    @Test
    void shouldReturnBadRequestWhenReservedIsNotBoolean() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(RESERVED, SOME_TEXT);

        GatewayResponse<String> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(RESERVED_FIELD_CAN_ONLY_BE_SET_TO_TRUE));
    }

    @Test
    void shouldReturnBadRequestWhenReservedIsFalse() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(RESERVED, false);

        GatewayResponse<String> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(RESERVED_FIELD_CAN_ONLY_BE_SET_TO_TRUE));
    }

    private static String randomIntegerAsString() {
        return String.valueOf(randomInteger());
    }

    private GatewayResponse<String> sendQuery(Map<String, String> pathParam, String body) throws IOException {
        InputStream input = createRequest(pathParam, body);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream createRequest(Map<String, String> pathParam, String body) throws JsonProcessingException {
        return new HandlerRequestBuilder<String>(OBJECT_MAPPER)
            .withBody(body)
            .withAccessRight(EDIT_OWN_INSTITUTION_USERS)
            .withPathParameters(pathParam)
            .build();
    }

    private GatewayResponse<String> queryWithoutRequiredAccessRights(String body) throws IOException {
        InputStream input = new HandlerRequestBuilder<String>(OBJECT_MAPPER)
            .withBody(body)
            .withPathParameters(validPath)
            .build();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output);
    }
}
