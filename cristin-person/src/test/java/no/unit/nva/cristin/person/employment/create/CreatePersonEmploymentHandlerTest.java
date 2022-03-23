package no.unit.nva.cristin.person.employment.create;

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
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreatePersonEmploymentHandlerTest {

    private static final Map<String, String> validPath = Map.of(PERSON_ID, randomIntegerAsString());

    private final Environment environment = new Environment();
    private CreatePersonEmploymentClient apiClient;
    private Context context;
    private ByteArrayOutputStream output;
    private CreatePersonEmploymentHandler handler;

    @BeforeEach
    void setUp() {
        apiClient = new CreatePersonEmploymentClient();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new CreatePersonEmploymentHandler(apiClient, environment);
    }

    @Test
    void shouldReturnDummyStatusCreatedWhenPassingAuthCheck() throws IOException {
        GatewayResponse<CristinPersonEmployment> gatewayResponse = sendQuery(validPath, new CristinPersonEmployment());

        assertEquals(HttpURLConnection.HTTP_CREATED, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenClientIsNotAuthenticated() throws IOException {
        GatewayResponse<CristinPersonEmployment> gatewayResponse = queryWithoutRequiredAccessRights();

        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    private static String randomIntegerAsString() {
        return String.valueOf(randomInteger());
    }

    private GatewayResponse<CristinPersonEmployment> sendQuery(Map<String, String> pathParam,
                                                               CristinPersonEmployment body)
        throws IOException {

        InputStream input = createRequest(pathParam, body);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream createRequest(Map<String, String> pathParam, CristinPersonEmployment body)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<CristinPersonEmployment>(OBJECT_MAPPER)
            .withBody(body)
            .withAccessRight(EDIT_OWN_INSTITUTION_USERS)
            .withPathParameters(pathParam)
            .build();
    }

    private GatewayResponse<CristinPersonEmployment> queryWithoutRequiredAccessRights() throws IOException {
        InputStream input = new HandlerRequestBuilder<CristinPersonEmployment>(OBJECT_MAPPER)
            .withBody(null)
            .withPathParameters(validPath)
            .build();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output);
    }
}
