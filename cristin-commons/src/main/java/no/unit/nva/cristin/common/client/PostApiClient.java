package no.unit.nva.cristin.common.client;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static no.unit.nva.cristin.model.Constants.CRISTIN_INSTITUTION_HEADER;
import com.google.common.net.MediaType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import no.unit.nva.cristin.common.ErrorMessages;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.exception.GatewayTimeoutException;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;

public class PostApiClient extends ApiClient {

    public static final String APPLICATION_JSON = MediaType.JSON_UTF_8.toString();

    private final transient HttpClient client;

    public PostApiClient(HttpClient client) {
        super(client);
        this.client = client;
    }

    /**
     * Initiate a synchronous POST to uri with supplied payload. Returns response from upstream.
     */
    public HttpResponse<String> post(URI uri, String body)
        throws GatewayTimeoutException, FailedHttpRequestException {

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(uri)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return getSuccessfulResponseOrThrowException(httpRequest);
    }

    /**
     * Initiate a synchronous POST to uri with supplied payload. Returns response from upstream.
     * Accepts header for Cristin Institution allowed to update.
     */
    public HttpResponse<String> post(URI uri, String body, String cristinInstitutionNumber)
        throws GatewayTimeoutException, FailedHttpRequestException {

        var httpRequest = HttpRequest.newBuilder()
                              .uri(uri)
                              .header(CONTENT_TYPE, APPLICATION_JSON)
                              .header(CRISTIN_INSTITUTION_HEADER, cristinInstitutionNumber)
                              .POST(HttpRequest.BodyPublishers.ofString(body))
                              .build();
        return getSuccessfulResponseOrThrowException(httpRequest);
    }

    /**
     * Initiate an asynchronous POST to uri with supplied payload. Returns response from upstream.
     */
    public CompletableFuture<HttpResponse<String>> postAsync(URI uri, String body) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header(CONTENT_TYPE, APPLICATION_JSON)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        return client.sendAsync(request, BodyHandlers.ofString());
    }

    /**
     * Checks for POST http error codes and also the regular error codes. Throws exception on error
     */
    protected void checkPostHttpStatusCode(URI uri, int statusCode) throws ApiGatewayException {
        if (isSuccessful(statusCode)) {
            return;
        }
        if (statusCode == HTTP_BAD_REQUEST) {
            throw new BadRequestException(ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD);
        }
        checkHttpStatusCode(uri, statusCode);
    }
}
