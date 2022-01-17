package no.unit.nva.cristin.common.client;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Map;
import no.unit.nva.cristin.common.ErrorMessages;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class PostApiClientTest {

    private static final String EMPTY_JSON = "{}";
    private static final URI DUMMY_URI = new UriWrapper("https://example.org").getUri();
    private static final Map<String, String> DUMMY_BODY = Map.of("hello", "world");

    private HttpClient clientMock;
    private PostApiClient apiClient;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        clientMock = mock(HttpClient.class);
        when(clientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 201));
        apiClient = new PostApiClient(clientMock);
    }

    @Test
    void shouldReturnDummyResponseOnPost() throws ApiGatewayException {
        HttpResponse<String> response = apiClient.fetchPostResult(DUMMY_URI, createJson());
        apiClient.checkHttpStatusCode(DUMMY_URI, response.statusCode());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
    }

    @Test
    void shouldThrowBadRequestOnPostWithErrorCode()
        throws ApiGatewayException, IOException, InterruptedException {

        when(clientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 400));
        apiClient = new PostApiClient(clientMock);
        HttpResponse<String> response = apiClient.fetchPostResult(DUMMY_URI, createJson());
        Executable action = () -> apiClient.checkPostHttpStatusCode(DUMMY_URI, response.statusCode());

        BadRequestException exception = assertThrows(BadRequestException.class, action);
        assertEquals(ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD, exception.getMessage());
    }

    private String createJson() {
        return attempt(() -> OBJECT_MAPPER.writeValueAsString(DUMMY_BODY)).orElseThrow();
    }
}
