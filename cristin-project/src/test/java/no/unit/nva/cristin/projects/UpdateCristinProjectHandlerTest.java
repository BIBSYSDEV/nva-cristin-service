package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static no.unit.nva.cristin.common.client.PatchApiClient.EMPTY_JSON;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.UpdateCristinProjectHandler.ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_USERS;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateCristinProjectHandlerTest {

    private static final String PROJECT_IDENTIFIER = "identifier";
    private static final Map<String, String> validPath = Map.of(PROJECT_IDENTIFIER, randomIntegerAsString());

    private final HttpClient httpClientMock = mock(HttpClient.class);
    private Context context;
    private ByteArrayOutputStream output;
    private UpdateCristinProjectHandler handler;


    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 204));
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new UpdateCristinProjectHandler();
    }


    @Test
    void shouldThrowForbiddenExceptionWhenClientIsNotAuthenticated() throws IOException {
        GatewayResponse<Void> gatewayResponse = queryWithoutRequiredAccessRights();

        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldReturnBadRequestWhenSendingNullBody() throws IOException {
        GatewayResponse<Void> gatewayResponse = sendQuery(validPath, null);
        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD));
    }


    private static String randomIntegerAsString() {
        return String.valueOf(randomInteger());
    }

    private GatewayResponse<Void> sendQuery(Map<String, String> pathParam, String body) throws IOException {
        InputStream input = createRequest(pathParam, body);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Void.class);
    }

    private InputStream createRequest(Map<String, String> pathParam, String body) throws JsonProcessingException {
        return new HandlerRequestBuilder<String>(OBJECT_MAPPER)
                .withBody(body)
                .withAccessRight(EDIT_OWN_INSTITUTION_USERS)
                .withPathParameters(pathParam)
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
}
