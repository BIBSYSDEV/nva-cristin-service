package no.unit.nva.cristin.common.client;

import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.attempt.Failure;
import nva.commons.core.attempt.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FAILED_WITH_STATUSCODE;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_IDENTIFIER_NOT_FOUND_FOR_URI;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_READING_RESPONSE_FAIL;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static nva.commons.core.attempt.Try.attempt;

public class ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);

    private static final int FIRST_NON_SUCCESS_CODE = 300;

    private final transient HttpClient client;

    public ApiClient(HttpClient client) {
        this.client = client;
    }

    /**
     * Build and perform GET request for given URI.
     * @param uri to fetch from
     * @return response containing data from requested URI or error
     */
    public CompletableFuture<HttpResponse<String>> fetchGetResultAsync(URI uri) {
        return client.sendAsync(
            HttpRequest.newBuilder(uri).GET().build(),
            BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    public HttpResponse<String> fetchGetResult(URI uri) {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).build();
        return attempt(() -> client.send(httpRequest, BodyHandlers.ofString(StandardCharsets.UTF_8))).orElseThrow();
    }

    public HttpResponse<String> fetchQueryResults(URI uri) {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).build();
        return attempt(() -> client.send(httpRequest, BodyHandlers.ofString(StandardCharsets.UTF_8))).orElseThrow();
    }

    public static <T> T fromJson(String body, Class<T> classOfT) throws IOException {
        return OBJECT_MAPPER.readValue(body, classOfT);
    }

    public long calculateProcessingTime(long startRequestTime, long endRequestTime) {
        return endRequestTime - startRequestTime;
    }

    protected  <T> T getDeserializedResponse(HttpResponse<String> response, Class<T> classOfT)
        throws BadGatewayException {

        return attempt(() -> fromJson(response.body(), classOfT))
            .orElseThrow(failure -> logAndThrowDeserializationError(response, failure));
    }

    private <T> BadGatewayException logAndThrowDeserializationError(HttpResponse<String> response, Failure<T> failure) {
        logError(ERROR_MESSAGE_READING_RESPONSE_FAIL, response.body(), failure.getException());
        return new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
    }

    protected void logError(String message, String data, Exception failure) {
        logger.error(String.format(message, data, failure.getMessage()));
    }

    /**
     * Get results for a list of URIs.
     * @param uris list containing URIs
     * @return responses for given URIs
     */
    public List<HttpResponse<String>> fetchQueryResultsOneByOne(List<URI> uris) {
        List<CompletableFuture<HttpResponse<String>>> responsesContainer =
            uris.stream().map(this::fetchGetResultAsync).collect(Collectors.toList());

        return collectSuccessfulResponsesOrThrowException(responsesContainer);
    }

    private List<HttpResponse<String>> collectSuccessfulResponsesOrThrowException(
        List<CompletableFuture<HttpResponse<String>>> responsesContainer) {

        return responsesContainer.stream()
            .map(attempt(CompletableFuture::get))
            .map(Try::orElseThrow)
            .filter(this::isSuccessfulRequest)
            .collect(Collectors.toList());
    }

    private boolean isSuccessfulRequest(HttpResponse<String> response) {
        try {
            checkHttpStatusCode(response.uri(), response.statusCode());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    protected void checkHttpStatusCode(URI uri, int statusCode)
        throws NotFoundException, BadGatewayException {

        String uriAsString = Optional.ofNullable(uri).map(URI::toString).orElse(EMPTY_STRING);

        if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            String msg = String.format(ERROR_MESSAGE_IDENTIFIER_NOT_FOUND_FOR_URI, uriAsString);
            throw new NotFoundException(msg);
        } else if (remoteServerHasInternalProblems(statusCode)) {
            logBackendFetchFail(uriAsString, statusCode);
            throw new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
        } else if (errorIsUnknown(statusCode)) {
            logBackendFetchFail(uriAsString, statusCode);
            throw new RuntimeException();
        }
    }

    private boolean errorIsUnknown(int statusCode) {
        return responseIsFailure(statusCode)
            && !remoteServerHasInternalProblems(statusCode);
    }

    private void logBackendFetchFail(String uri, int statusCode) {
        logger.error(String.format(ERROR_MESSAGE_BACKEND_FAILED_WITH_STATUSCODE, statusCode, uri));
    }

    private boolean responseIsFailure(int statusCode) {
        return statusCode >= FIRST_NON_SUCCESS_CODE;
    }

    private boolean remoteServerHasInternalProblems(int statusCode) {
        return statusCode >= HttpURLConnection.HTTP_INTERNAL_ERROR;
    }
}
