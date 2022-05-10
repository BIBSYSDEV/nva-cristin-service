package no.unit.nva.cristin.person.update;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;
import java.net.URI;
import no.unit.nva.cristin.common.ErrorMessages;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD;
import static no.unit.nva.cristin.common.client.PatchApiClient.EMPTY_JSON;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_FIRST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.FIELD_CAN_NOT_BE_ERASED;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.ORCID_IS_NOT_VALID;
import static no.unit.nva.cristin.person.update.PersonPatchValidator.RESERVED_FIELD_CAN_ONLY_BE_SET_TO_TRUE;
import static no.unit.nva.cristin.person.update.UpdateCristinPersonHandler.ERROR_MESSAGE_IDENTIFIERS_DO_NOT_MATCH;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_USERS;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpdateCristinPersonHandlerTest {

    private static final String PERSON_CRISTIN_ID = "123456";
    private static final URI PERSON_CRISTIN_ID_URI = UriWrapper.fromUri(randomUri()).addChild(PERSON_CRISTIN_ID)
                                                         .getUri();
    private static final Map<String, String> validPath = Map.of(PERSON_ID, PERSON_CRISTIN_ID);
    private static final String VALID_ORCID = "1234-1234-1234-1234";
    private static final String INVALID_ORCID = "1234";
    private static final String SOME_TEXT = "Hello";
    public static final String UNSUPPORTED_FIELD = "unsupportedField";
    public static final String INVALID_IDENTIFIER = "hello";
    private static final String IDENTIFIER_NOT_MATCHING_COGNITO = "555666";

    private final HttpClient httpClientMock = mock(HttpClient.class);
    private UpdateCristinPersonApiClient apiClient;
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private UpdateCristinPersonHandler handler;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 204));
        apiClient = new UpdateCristinPersonApiClient(httpClientMock);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new UpdateCristinPersonHandler(apiClient, environment);
    }

    @Test
    void shouldReturnNoContentResponseWhenCallingHandlerWithValidJson() throws IOException {
        ObjectNode jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(ORCID, VALID_ORCID);
        jsonObject.put(FIRST_NAME, randomString());
        jsonObject.put(PREFERRED_FIRST_NAME, randomString());
        jsonObject.putNull(PREFERRED_LAST_NAME);

        GatewayResponse<Void> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenClientIsNotAuthenticated() throws IOException {
        GatewayResponse<Void> gatewayResponse = queryWithoutRequiredAccessRights();

        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldReturnBadRequestWhenSendingNullBody() throws IOException {
        GatewayResponse<Void> gatewayResponse = sendQuery(validPath, null);

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_PAYLOAD));
    }

    @Test
    void shouldReturnOkNoContentWhenOrcidIsPresentAndNull() throws IOException {
        ObjectNode jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.putNull(ORCID);

        GatewayResponse<Void> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenOrcidHasInvalidValue() throws IOException {
        ObjectNode jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(ORCID, INVALID_ORCID);

        GatewayResponse<Void> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ORCID_IS_NOT_VALID));
    }

    @Test
    void shouldReturnBadRequestWhenPrimaryNameIsNull() throws IOException {
        ObjectNode jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.putNull(FIRST_NAME);

        GatewayResponse<Void> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(String.format(FIELD_CAN_NOT_BE_ERASED, FIRST_NAME)));
    }

    @Test
    void shouldReturnBadRequestWhenReservedIsNull() throws IOException {
        ObjectNode jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.putNull(RESERVED);

        GatewayResponse<Void> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(RESERVED_FIELD_CAN_ONLY_BE_SET_TO_TRUE));
    }

    @Test
    void shouldReturnBadRequestWhenReservedIsNotBoolean() throws IOException {
        ObjectNode jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(RESERVED, SOME_TEXT);

        GatewayResponse<Void> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(RESERVED_FIELD_CAN_ONLY_BE_SET_TO_TRUE));
    }

    @Test
    void shouldReturnBadRequestWhenReservedIsFalse() throws IOException {
        ObjectNode jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(RESERVED, false);

        GatewayResponse<Void> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(RESERVED_FIELD_CAN_ONLY_BE_SET_TO_TRUE));
    }

    @Test
    void shouldReturnBadRequestWhenNoSupportedFieldsArePresent() throws IOException {
        ObjectNode jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(UNSUPPORTED_FIELD, randomString());

        GatewayResponse<Void> gatewayResponse = sendQuery(validPath, jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidIdentifier() throws IOException {
        ObjectNode jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(ORCID, VALID_ORCID);

        GatewayResponse<Void> gatewayResponse = sendQuery(Map.of(PERSON_ID, INVALID_IDENTIFIER),
            jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ErrorMessages.invalidPathParameterMessage(PERSON_ID)));
    }

    @Test
    void shouldReturnNoContentResponseWhenUserIsUpdatingTheirOwnPersonDataThatTheyAreAllowedToUpdate()
        throws IOException {
        ObjectNode jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(ORCID, VALID_ORCID);
        GatewayResponse<Void> gatewayResponse = queryWithPersonCristinIdButNoAccessRights(validPath,
                                                                                          jsonObject.toString());

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldThrowBadRequestWhenUsersWantToUpdateTheirOwnPersonDataButSendsFieldsTheyAreNotAllowedToUpdate()
        throws IOException {
        ObjectNode jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(FIRST_NAME, randomString());
        jsonObject.put(LAST_NAME, randomString());
        jsonObject.put(PREFERRED_FIRST_NAME, randomString());
        jsonObject.putNull(PREFERRED_LAST_NAME);
        jsonObject.put(RESERVED, true);
        GatewayResponse<Void> gatewayResponse = queryWithPersonCristinIdButNoAccessRights(validPath,
                                                                                          jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD));
    }

    @Test
    void shouldThrowBadRequestWhenPathPersonIdentifierNotMatchingCognitoPersonIdentifier()
        throws IOException {
        ObjectNode jsonObject = OBJECT_MAPPER.createObjectNode();
        jsonObject.put(ORCID, VALID_ORCID);
        GatewayResponse<Void> gatewayResponse =
            queryWithPersonCristinIdButNoAccessRights(Map.of(PERSON_ID, IDENTIFIER_NOT_MATCHING_COGNITO),
                                                      jsonObject.toString());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_IDENTIFIERS_DO_NOT_MATCH));
    }

    private GatewayResponse<Void> sendQuery(Map<String, String> pathParam, String body) throws IOException {
        InputStream input = createRequest(pathParam, body);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Void.class);
    }

    private InputStream createRequest(Map<String, String> pathParam, String body) throws JsonProcessingException {
        var customerId = randomUri();
        return new HandlerRequestBuilder<String>(OBJECT_MAPPER)
            .withBody(body)
            .withPathParameters(pathParam)
            .withCustomerId(customerId)
            .withAccessRights(customerId, EDIT_OWN_INSTITUTION_USERS)
            .build();
    }

    private GatewayResponse<Void> queryWithoutRequiredAccessRights() throws IOException {
        InputStream input = new HandlerRequestBuilder<String>(OBJECT_MAPPER)
            .withBody(EMPTY_JSON)
            .withPathParameters(validPath)
            .build();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output, Void.class);
    }

    private GatewayResponse<Void> queryWithPersonCristinIdButNoAccessRights(Map<String, String> pathParameters,
                                                                            String body) throws IOException {
        InputStream input = new HandlerRequestBuilder<String>(OBJECT_MAPPER)
                                .withBody(body)
                                .withPathParameters(pathParameters)
                                .withPersonCristinId(PERSON_CRISTIN_ID_URI)
                                .build();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output, Void.class);
    }
}
