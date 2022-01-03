package no.unit.nva.cristin.common.client;

import no.unit.nva.model.Organization;
import no.unit.nva.utils.UriUtils;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FAILED_WITH_STATUSCODE;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_IDENTIFIER_NOT_FOUND_FOR_URI;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_READING_RESPONSE_FAIL;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static nva.commons.core.attempt.Try.attempt;

public class ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);

    private static final int FIRST_NON_SUCCESS_CODE = 300;

    public static final int FIRST_EFFORT = 0;
    public static final int MAX_EFFORTS = 2;
    public static final int WAITING_TIME = 500; //500 milliseconds
    public static final String LOG_INTERRUPTION = "InterruptedException while waiting to resend HTTP request";

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
        HttpRequest httpRequest = HttpRequest.newBuilder(UriUtils.addLanguage(uri)).build();
        return attempt(() -> client.send(httpRequest, BodyHandlers.ofString(StandardCharsets.UTF_8))).orElseThrow();
    }

    public HttpResponse<String> fetchQueryResults(URI uri) {
        HttpRequest httpRequest = HttpRequest.newBuilder(UriUtils.addLanguage(uri)).build();
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

    public Try<HttpResponse<String>> sendRequestMultipleTimes(URI uri) {
        Try<HttpResponse<String>> lastEffort = null;
        for (int effortCount = FIRST_EFFORT; shouldKeepTrying(effortCount, lastEffort); effortCount++) {
            waitBeforeRetrying(effortCount);
            lastEffort = attemptFetch(uri, effortCount);
        }
        return lastEffort;
    }

    private Try<HttpResponse<String>> attemptFetch(URI uri, int effortCount) {
        Try<HttpResponse<String>> newEffort = attempt(() -> fetchGetResultAsync(uri).get());
        if (newEffort.isFailure()) {
            logger.warn(String.format("Failed HttpRequest on attempt %d of 3: ", effortCount + 1)
                    + newEffort.getException().getMessage(), newEffort.getException()
            );
        }
        return newEffort;
    }

    private boolean shouldTryMoreTimes(int effortCount) {
        return effortCount < MAX_EFFORTS;
    }

    @SuppressWarnings("PMD.UselessParentheses") // keep the parenthesis for clarity
    private boolean shouldKeepTrying(int effortCount, Try<HttpResponse<String>> lastEffort) {
        return lastEffort == null || (lastEffort.isFailure() && shouldTryMoreTimes(effortCount));
    }

    private int waitBeforeRetrying(int effortCount) {
        if (effortCount > FIRST_EFFORT) {
            try {
                Thread.sleep(WAITING_TIME);
            } catch (InterruptedException e) {
                logger.error(LOG_INTERRUPTION);
                throw new RuntimeException(e);
            }
        }
        return effortCount;
    }

    public Integer calculateFirstRecord(Map<String, String> requestQueryParams) {
        final int page = Integer.parseInt(requestQueryParams.get(PAGE));
        final int pageSize = Integer.parseInt(requestQueryParams.get(NUMBER_OF_RESULTS));
        return (page - 1) * pageSize + 1;
    }

    public int getCount(HttpResponse<String> response, List<Organization> organizations) {
        return response.headers().firstValue("X-Total-Count").isPresent()
                ? Integer.parseInt(response.headers().firstValue("X-Total-Count").get())
                : organizations.size();
    }

    public boolean isSuccessful(int statusCode) {
        return statusCode <= HTTP_MULT_CHOICE && statusCode >= HTTP_OK;
    }

}
