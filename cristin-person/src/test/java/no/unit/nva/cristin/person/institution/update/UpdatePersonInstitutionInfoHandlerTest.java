package no.unit.nva.cristin.person.institution.update;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.person.institution.update.UpdatePersonInstitutionInfoClient.EMPTY_JSON;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_USERS;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.util.Map;
import no.unit.nva.cristin.person.model.nva.PersonInstInfoPatch;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UpdatePersonInstitutionInfoHandlerTest {

    private static final String PERSON_ID = "id";
    private static final String ORG_ID = "orgId";
    private static final Map<String, String> validPath =
        Map.of(PERSON_ID, randomIntegerAsString(), ORG_ID, randomIntegerAsString());

    private final HttpClient httpClientMock = mock(HttpClient.class);
    private UpdatePersonInstitutionInfoClient apiClient;
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private UpdatePersonInstitutionInfoHandler handler;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 204));
        apiClient = new UpdatePersonInstitutionInfoClient(httpClientMock);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new UpdatePersonInstitutionInfoHandler(apiClient, environment);
    }

    @Test
    void shouldReturnNoContentResponseWhenCallingHandlerWithValidData() throws IOException {
        GatewayResponse<Object> gatewayResponse = sendQuery(validPath, defaultBody());

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
        assertEquals(EMPTY_JSON, gatewayResponse.getBody());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenClientIsNotAuthenticated() throws IOException {
        GatewayResponse<String> gatewayResponse = queryWithoutRequiredAccessRights(defaultBody());

        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldThrowBadRequestIfNoSupportedFieldsIsPresentInRequest() throws IOException {
        GatewayResponse<Object> gatewayResponse = sendQuery(validPath, bodyWithUnsupportedFields());

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD));
    }

    @Test
    void shouldReturnBadRequestWhenSendingNullBody() throws IOException {
        GatewayResponse<Object> gatewayResponse = sendQuery(validPath, null);

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
    }

    private static String randomIntegerAsString() {
        return String.valueOf(randomInteger());
    }

    private GatewayResponse<Object> sendQuery(Map<String, String> pathParam, PersonInstInfoPatch body)
        throws IOException {

        InputStream input = createRequest(pathParam, body);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Object.class);
    }

    private InputStream createRequest(Map<String, String> pathParam, PersonInstInfoPatch body)
        throws JsonProcessingException {
        var customerId = randomUri();
        return new HandlerRequestBuilder<PersonInstInfoPatch>(OBJECT_MAPPER)
            .withBody(body)
            .withCustomerId(customerId)
            .withAccessRights(customerId, EDIT_OWN_INSTITUTION_USERS)
            .withPathParameters(pathParam)
            .build();
    }

    private GatewayResponse<String> queryWithoutRequiredAccessRights(PersonInstInfoPatch body) throws IOException {
        InputStream input = new HandlerRequestBuilder<PersonInstInfoPatch>(OBJECT_MAPPER)
            .withBody(body)
            .withPathParameters(validPath)
            .build();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output, String.class);
    }

    private PersonInstInfoPatch defaultBody() throws JsonProcessingException {
        String body = "{\"email\":\"test@example.com\", \"phone\":\"99112233\"}";
        return OBJECT_MAPPER.readValue(body, PersonInstInfoPatch.class);
    }

    private PersonInstInfoPatch bodyWithUnsupportedFields() throws JsonProcessingException {
        String body = "{\"hello\":\"world\", \"lorem\":\"ipsum\"}";
        return OBJECT_MAPPER.readValue(body, PersonInstInfoPatch.class);
    }
}
