package no.unit.nva.cristin.person.employment.create;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import java.net.URI;
import java.net.URISyntaxException;
import no.unit.nva.cristin.common.ErrorMessages;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Map;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_USERS;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreatePersonEmploymentHandlerTest {

    private static final Map<String, String> validPath = Map.of(PERSON_ID, randomIntegerAsString());
    private static final String EMPTY_JSON = "{}";
    private static final String validJson =
        IoUtils.stringFromResources(Path.of("nvaApiCreateEmploymentRequest.json"));
    private static final String INVALID_URI = "https://example.org/hello";

    private final HttpClient httpClientMock = mock(HttpClient.class);
    private final Environment environment = new Environment();
    private CreatePersonEmploymentClient apiClient;
    private Context context;
    private ByteArrayOutputStream output;
    private CreatePersonEmploymentHandler handler;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 201));
        apiClient = new CreatePersonEmploymentClient(httpClientMock);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new CreatePersonEmploymentHandler(apiClient, environment);
    }

    @Test
    void shouldReturnStatusCreatedWhenSendingValidPayload() throws IOException {
        Employment employment = OBJECT_MAPPER.readValue(validJson, Employment.class);
        GatewayResponse<Employment> gatewayResponse = sendQuery(validPath, employment);

        assertEquals(HttpURLConnection.HTTP_CREATED, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenClientIsNotAuthenticated() throws IOException {
        GatewayResponse<Employment> gatewayResponse = queryWithoutRequiredAccessRights();

        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldThrowBadRequestWhenInvalidPositionCode() throws IOException, URISyntaxException {
        Employment employment = OBJECT_MAPPER.readValue(validJson, Employment.class);
        employment.setType(new URI(INVALID_URI));
        GatewayResponse<Employment> gatewayResponse = sendQuery(validPath, employment);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ErrorMessages.invalidFieldParameterMessage(TYPE)));
    }

    @Test
    void shouldThrowBadRequestWhenInvalidAffiliation() throws IOException, URISyntaxException {
        Employment employment = OBJECT_MAPPER.readValue(validJson, Employment.class);
        employment.setOrganization(new URI(INVALID_URI));
        GatewayResponse<Employment> gatewayResponse = sendQuery(validPath, employment);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ErrorMessages.invalidFieldParameterMessage(ORGANIZATION)));
    }

    private static String randomIntegerAsString() {
        return String.valueOf(randomInteger());
    }

    private GatewayResponse<Employment> sendQuery(Map<String, String> pathParam, Employment body) throws IOException {
        InputStream input = createRequest(pathParam, body);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Employment.class);
    }

    private InputStream createRequest(Map<String, String> pathParam, Employment body)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<Employment>(OBJECT_MAPPER)
            .withBody(body)
            .withAccessRight(EDIT_OWN_INSTITUTION_USERS)
            .withPathParameters(pathParam)
            .build();
    }

    private GatewayResponse<Employment> queryWithoutRequiredAccessRights() throws IOException {
        InputStream input = new HandlerRequestBuilder<Employment>(OBJECT_MAPPER)
            .withBody(null)
            .withPathParameters(validPath)
            .build();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output, Employment.class);
    }
}
