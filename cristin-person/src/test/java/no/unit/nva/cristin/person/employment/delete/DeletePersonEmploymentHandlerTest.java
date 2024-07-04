package no.unit.nva.cristin.person.employment.delete;

import static no.unit.nva.cristin.model.Constants.EMPLOYMENT_ID;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.AccessRight.MANAGE_OWN_AFFILIATION;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
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
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeletePersonEmploymentHandlerTest {

    private static final String EMPTY_JSON = "{}";
    private static final String INVALID_PERSON_ID = "abc";
    private static final String INVALID_EMPLOYMENT_ID = "def";
    private static final String VALID_PERSON_ID = "123456789";
    private static final String VALID_EMPLOYMENT_ID = "987654321";
    private final HttpClient httpClientMock = mock(HttpClient.class);
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private DeletePersonEmploymentHandler handler;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 200));
        var apiClient = new DeletePersonEmploymentClient(httpClientMock);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new DeletePersonEmploymentHandler(apiClient, environment);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenClientIsNotAuthenticated() throws IOException {
        handler.handleRequest(getInputStreamRequestWithoutAuthentication(), output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output, Void.class);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenInvalidPersonId() throws IOException {
        handler.handleRequest(getInputStreamRequestWithInvalidPersonId(), output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output, Void.class);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenInvalidEmploymentId() throws IOException {
        handler.handleRequest(getInputStreamRequestWithInvalidEmploymentId(), output, context);
        var gatewayResponse = GatewayResponse.fromOutputStream(output, Void.class);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    private InputStream getInputStreamRequestWithoutAuthentication() throws JsonProcessingException {
        return new HandlerRequestBuilder<CristinPersonEmployment>(OBJECT_MAPPER)
            .withBody(null)
            .withPathParameters(null)
            .build();
    }

    private InputStream getInputStreamRequestWithInvalidPersonId() throws JsonProcessingException {
        var customerId = randomUri();

        return new HandlerRequestBuilder<CristinPersonEmployment>(OBJECT_MAPPER)
            .withCurrentCustomer(customerId)
            .withAccessRights(customerId, MANAGE_OWN_AFFILIATION)
            .withBody(null)
            .withPathParameters(Map.of(PERSON_ID, INVALID_PERSON_ID, EMPLOYMENT_ID, VALID_EMPLOYMENT_ID))
            .build();
    }

    private InputStream getInputStreamRequestWithInvalidEmploymentId() throws JsonProcessingException {
        var customerId = randomUri();

        return new HandlerRequestBuilder<CristinPersonEmployment>(OBJECT_MAPPER)
            .withCurrentCustomer(customerId)
            .withAccessRights(customerId, MANAGE_OWN_AFFILIATION)
            .withBody(null)
            .withPathParameters(Map.of(PERSON_ID, VALID_PERSON_ID, EMPLOYMENT_ID, INVALID_EMPLOYMENT_ID))
            .build();
    }
}
