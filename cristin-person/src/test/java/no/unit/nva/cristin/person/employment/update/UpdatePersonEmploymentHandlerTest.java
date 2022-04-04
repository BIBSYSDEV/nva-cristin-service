package no.unit.nva.cristin.person.employment.update;

import static no.unit.nva.cristin.common.client.PatchApiClient.EMPTY_JSON;
import static no.unit.nva.cristin.model.Constants.EMPLOYMENT_ID;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_USERS;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UpdatePersonEmploymentHandlerTest {

    private static final Map<String, String> validPath =
        Map.of(PERSON_ID, randomIntegerAsString(), EMPLOYMENT_ID, randomIntegerAsString());

    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private UpdatePersonEmploymentHandler handler;

    @BeforeEach
    void setUp() {
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new UpdatePersonEmploymentHandler(environment);
    }

    @Test
    void shouldReturnNoContentResponseWhenClientIsAuthenticated() throws IOException {
        GatewayResponse<Void> gatewayResponse = sendQuery(validPath, EMPTY_JSON);

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenClientIsNotAuthenticated() throws IOException {
        GatewayResponse<Void> gatewayResponse = queryWithoutRequiredAccessRights();

        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    private static String randomIntegerAsString() {
        return String.valueOf(randomInteger());
    }

    private GatewayResponse<Void> sendQuery(Map<String, String> pathParam, String body) throws IOException {
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

    private GatewayResponse<Void> queryWithoutRequiredAccessRights() throws IOException {
        InputStream input = new HandlerRequestBuilder<String>(OBJECT_MAPPER)
            .withBody(EMPTY_JSON)
            .withPathParameters(validPath)
            .build();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output);
    }
}
