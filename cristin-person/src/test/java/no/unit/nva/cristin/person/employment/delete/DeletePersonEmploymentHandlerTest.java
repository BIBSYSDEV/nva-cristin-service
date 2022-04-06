package no.unit.nva.cristin.person.employment.delete;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.util.Map;

import static no.unit.nva.cristin.model.Constants.EMPLOYMENT_ID;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_USERS;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeletePersonEmploymentHandlerTest {

    private static final String EMPTY_JSON = "{}";
    private static final String INVALID_PERSON_ID = "abc";
    private static final String INVALID_EMPLOYMENT_ID = "def";
    private static final String VALID_PERSON_ID = "123456789";
    private static final String VALID_EMPLOYMENT_ID = "987654321";
    private final HttpClient httpClientMock = mock(HttpClient.class);
    private final Environment environment = new Environment();
    private DeletePersonEmploymentClient apiClient;
    private Context context;
    private ByteArrayOutputStream output;
    private DeletePersonEmploymentHandler handler;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 200));
        apiClient = new DeletePersonEmploymentClient(httpClientMock);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new DeletePersonEmploymentHandler(apiClient, environment);
    }


    @Test
    void shouldThrowForbiddenExceptionWhenClientIsNotAuthenticated() throws IOException {
        handler.handleRequest(getInputStreamRequestWithoutAuthentication(), output, context);
        GatewayResponse<Void> gatewayResponse = GatewayResponse.fromOutputStream(output, Void.class);
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenInvalidPersonId() throws IOException {
        handler.handleRequest(getInputStreamRequestWithInvalidPersonId(), output, context);
        GatewayResponse<Void> gatewayResponse = GatewayResponse.fromOutputStream(output, Void.class);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenInvalidEmploymentId() throws IOException {
        handler.handleRequest(getInputStreamRequestWithInvalidEmploymentId(), output, context);
        GatewayResponse<Void> gatewayResponse = GatewayResponse.fromOutputStream(output, Void.class);
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
        return new HandlerRequestBuilder<CristinPersonEmployment>(OBJECT_MAPPER)
                .withAccessRight(EDIT_OWN_INSTITUTION_USERS)
                .withBody(null)
                .withPathParameters(Map.of(PERSON_ID, INVALID_PERSON_ID, EMPLOYMENT_ID, VALID_EMPLOYMENT_ID))
                .build();
    }

    private InputStream getInputStreamRequestWithInvalidEmploymentId() throws JsonProcessingException {
        return new HandlerRequestBuilder<CristinPersonEmployment>(OBJECT_MAPPER)
                .withAccessRight(EDIT_OWN_INSTITUTION_USERS)
                .withBody(null)
                .withPathParameters(Map.of(PERSON_ID, VALID_PERSON_ID, EMPLOYMENT_ID, INVALID_EMPLOYMENT_ID))
                .build();
    }
}
