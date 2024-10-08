package no.unit.nva.cristin.common.client;

import io.github.resilience4j.retry.Retry;
import java.util.function.Supplier;
import no.unit.nva.cristin.common.ErrorMessages;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.exception.UnauthorizedException;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
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

import static io.vavr.control.Try.of;
import static io.vavr.control.Try.ofSupplier;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.client.RetryConfigProvider.defaultRetryRegistry;
import static no.unit.nva.client.RetryConfigProvider.defaultRetryRegistryAsync;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FAILED_WITH_EXCEPTION;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.common.client.CristinAuthenticator.basicAuthHeader;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME;
import static no.unit.nva.cristin.model.Constants.CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.utils.UriUtils.addLanguage;
import static no.unit.nva.utils.UriUtils.maskSensitiveData;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static nva.commons.core.attempt.Try.attempt;

@SuppressWarnings("PMD.GodClass")
public class ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);

    private static final int FIRST_NON_SUCCESS_CODE = 300;

    public static final int FIRST_EFFORT = 0;
    public static final int MAX_EFFORTS = 2;
    public static final int WAITING_TIME = 500; //500 milliseconds
    public static final String LOG_INTERRUPTION = "InterruptedException while waiting to resend HTTP request";
    public static final String AUTHORIZATION = "Authorization";
    public static final String FORBIDDEN_THIS_MIGHT_BE_AN_CONFIGURATION_ERROR =
        "Upstream returned 403 Forbidden. This might be an configuration error or missing or incorrect upstream "
        + "allow/bypass header";
    public static final String RETURNED_403_FORBIDDEN_TRY_AGAIN_LATER =
        "Upstream returned 403 Forbidden. Try again later";

    private final transient HttpClient client;

    public ApiClient(HttpClient client) {
        this.client = client;
    }

    /**
     * Build and perform asynchronous GET request for given URI.
     * @param uri to fetch from
     * @return response containing data from requested URI or error
     */
    public CompletableFuture<HttpResponse<String>> fetchGetResultAsync(URI uri) {
        var httpRequest = HttpRequest.newBuilder(addLanguage(uri))
                              .GET()
                              .header(CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME, CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE)
                              .build();

        return fetchAsyncResponseWithRetry(httpRequest);
    }

    /**
     * Build and perform asynchronous GET request for given URI with authentication header.
     * @param uri to fetch from
     * @return response containing data from requested URI or error
     */
    public CompletableFuture<HttpResponse<String>> authenticatedFetchGetResultAsync(URI uri) {
        var httpRequest = HttpRequest.newBuilder(addLanguage(uri))
                              .GET()
                              .header(AUTHORIZATION, basicAuthHeader())
                              .header(CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME, CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE)
                              .build();

        return fetchAsyncResponseWithRetry(httpRequest);
    }

    private CompletableFuture<HttpResponse<String>> fetchAsyncResponseWithRetry(HttpRequest httpRequest) {
        var retryRegistryAsync = defaultRetryRegistryAsync();
        var retryWithDefaultConfigAsync = retryRegistryAsync.retry("executeRequestAsync");
        Supplier<CompletableFuture<HttpResponse<String>>> supplier = () -> executeRequestAsync(httpRequest);

        return ofSupplier(Retry.decorateSupplier(retryWithDefaultConfigAsync, supplier)).get();
    }

    private CompletableFuture<HttpResponse<String>> executeRequestAsync(HttpRequest httpRequest) {
        return of(() -> client.sendAsync(httpRequest, BodyHandlers.ofString(StandardCharsets.UTF_8))).get();
    }

    /**
     * Build and perform blocking synchronous GET request for given URI.
     * @param uri to fetch from
     * @return response containing data from requested URI or error
     */
    public HttpResponse<String> fetchGetResult(URI uri) throws ApiGatewayException {
        HttpRequest httpRequest = HttpRequest.newBuilder(addLanguage(uri))
                                      .header(CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME,
                                              CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE)
                                      .build();
        return getSuccessfulResponseOrThrowException(httpRequest);
    }

    /**
     * Build and perform blocking synchronous GET request for given URI with authentication header.
     * @param uri to fetch from
     * @return response containing data from requested URI or error
     */
    public HttpResponse<String> fetchGetResultWithAuthentication(URI uri) throws ApiGatewayException {
        HttpRequest httpRequest = HttpRequest.newBuilder(addLanguage(uri))
                                      .header(AUTHORIZATION, basicAuthHeader())
                                      .header(CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME,
                                              CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE)
                                      .build();
        return getSuccessfulResponseOrThrowException(httpRequest);
    }

    /**
     * Build and perform blocking synchronous GET request for given URI.
     * @param uri to fetch from
     * @return response containing data from requested URI or error
     */
    public HttpResponse<String> fetchQueryResults(URI uri) throws ApiGatewayException {
        HttpRequest httpRequest = HttpRequest.newBuilder(addLanguage(uri))
                                      .header(CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME,
                                              CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE)
                                      .build();
        return getSuccessfulResponseOrThrowException(httpRequest);
    }

    protected HttpResponse<String> getSuccessfulResponseOrThrowException(HttpRequest httpRequest)
        throws FailedHttpRequestException {

        try {
            return fetchResponseWithRetry(httpRequest);
        } catch (Exception ex) {
            var uri = maskSensitiveData(httpRequest.uri());
            logError(ERROR_MESSAGE_BACKEND_FAILED_WITH_EXCEPTION, uri, ex);
            throw new FailedHttpRequestException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
        }
    }

    private HttpResponse<String> fetchResponseWithRetry(HttpRequest httpRequest) {
        var retryRegistry = defaultRetryRegistry();
        var retryWithDefaultConfig = retryRegistry.retry("executeRequest");
        Supplier<HttpResponse<String>> supplier = () -> executeRequest(httpRequest);

        return ofSupplier(Retry.decorateSupplier(retryWithDefaultConfig, supplier))
                   .getOrElseThrow(throwable -> new RuntimeException(throwable.getMessage()));
    }

    private HttpResponse<String> executeRequest(HttpRequest httpRequest) {
        return of(() -> client.send(httpRequest, BodyHandlers.ofString(StandardCharsets.UTF_8)))
                   .getOrElseThrow(throwable -> new RuntimeException(throwable.getMessage()));
    }

    /**
     * Parse json string into model class.
     */
    public static <T> T fromJson(String body, Class<T> classOfT) throws IOException {
        return OBJECT_MAPPER.readValue(body, classOfT);
    }

    /**
     * Calculates processing time in milliseconds between two inputs based on System.currentTimeMillis()
     */
    public long calculateProcessingTime(long startRequestTime, long endRequestTime) {
        return endRequestTime - startRequestTime;
    }

    protected <T> T getDeserializedResponse(HttpResponse<String> response, Class<T> classOfT)
        throws BadGatewayException {

        return attempt(() -> fromJson(response.body(), classOfT))
            .orElseThrow(failure -> logAndThrowDeserializationError(response, failure));
    }

    private <T> BadGatewayException logAndThrowDeserializationError(HttpResponse<String> response, Failure<T> failure) {
        logError(ErrorMessages.ERROR_MESSAGE_READING_RESPONSE_FAIL, response.body(), failure.getException());
        return new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
    }

    protected void logError(String message, String data, Exception failure) {
        logger.error(String.format(message, data, failure.getClass().getCanonicalName() + ": " + failure.getMessage()));
    }

    /**
     * Get results for a list of URIs.
     * @param uris list containing URIs
     * @return responses for given URIs
     */
    public List<HttpResponse<String>> fetchQueryResultsOneByOne(List<URI> uris) {
        List<CompletableFuture<HttpResponse<String>>> responsesContainer =
            uris.stream().map(this::fetchGetResultAsync).collect(Collectors.toList());

        return collectSuccessfulResponses(responsesContainer);
    }

    /**
     * Get results for a list of URIs with authentication.
     * @param uris list containing URIs
     * @return responses for given URIs
     */
    public List<HttpResponse<String>> authorizedFetchQueryResultsOneByOne(List<URI> uris) {
        List<CompletableFuture<HttpResponse<String>>> responsesContainer =
                uris.stream().map(this::authenticatedFetchGetResultAsync).collect(Collectors.toList());

        return collectSuccessfulResponses(responsesContainer);
    }

    private List<HttpResponse<String>> collectSuccessfulResponses(
        List<CompletableFuture<HttpResponse<String>>> responsesContainer) {

        return responsesContainer.stream()
                   .map(attempt(CompletableFuture::get))
                   .filter(Try::isSuccess)
                   .map(Try::get)
                   .filter(this::isSuccessfulRequest)
                   .toList();
    }

    // This is reported as unused, but it is…
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private boolean isSuccessfulRequest(HttpResponse<String> response) {
        try {
            checkHttpStatusCode(response.uri(), response.statusCode(), response.body());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    protected void checkHttpStatusCode(URI uri, int statusCode, String body)
            throws NotFoundException, BadGatewayException, BadRequestException, UnauthorizedException {

        if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            var msg = String.format(ErrorMessages.ERROR_MESSAGE_IDENTIFIER_NOT_FOUND_FOR_URI, getUriAsString(uri));
            throw new NotFoundException(msg);
        } else if (statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new BadRequestException(ErrorMessages.UPSTREAM_RETURNED_BAD_REQUEST);
        } else if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new UnauthorizedException();
        } else if (statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
            logger.warn(FORBIDDEN_THIS_MIGHT_BE_AN_CONFIGURATION_ERROR);
            throw new BadGatewayException(RETURNED_403_FORBIDDEN_TRY_AGAIN_LATER);
        } else if (remoteServerHasInternalProblems(statusCode)) {
            logBackendFetchFail(getUriAsString(uri), statusCode, body);
            throw new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
        } else if (errorIsUnknown(statusCode)) {
            logBackendFetchFail(getUriAsString(uri), statusCode, body);
            throw new RuntimeException();
        }
    }

    private String getUriAsString(URI uri) {
        return Optional.ofNullable(uri).map(URI::toString).orElse(EMPTY_STRING);
    }

    private boolean errorIsUnknown(int statusCode) {
        return responseIsFailure(statusCode)
            && !remoteServerHasInternalProblems(statusCode);
    }

    private void logBackendFetchFail(String uri, int statusCode, String body) {
        logger.error(String.format(ErrorMessages.ERROR_MESSAGE_BACKEND_FAILED_WITH_STATUSCODE, statusCode, uri, body));
    }

    private boolean responseIsFailure(int statusCode) {
        return statusCode >= FIRST_NON_SUCCESS_CODE;
    }

    private boolean remoteServerHasInternalProblems(int statusCode) {
        return statusCode >= HttpURLConnection.HTTP_INTERNAL_ERROR;
    }

    /**
     * Send a request multiple times before failure.
     * @param uri location of wanted resource
     * @return Response with operation status and relevant content
     */
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

    /**
     * calculate value of firstRecord from requestParameters.
     * @param requestQueryParams parameters limiting this request
     * @return index of first record in resultSet
     */
    public Integer calculateFirstRecord(Map<String, String> requestQueryParams) {
        final int page = Integer.parseInt(requestQueryParams.get(PAGE));
        final int pageSize = Integer.parseInt(requestQueryParams.get(NUMBER_OF_RESULTS));
        return (page - 1) * pageSize + 1;
    }

    /**
     * report total number of results for this query.
     * @param response containing result for given parameters
     * @param items matching given criteria
     * @return total number of hits for this query
     */
    public int getCount(HttpResponse<String> response, List<?> items) {
        return response.headers().firstValue("X-Total-Count").isPresent()
                ? Integer.parseInt(response.headers().firstValue("X-Total-Count").get())
                : items.size();
    }

    public boolean isSuccessful(int statusCode) {
        return statusCode <= HTTP_MULT_CHOICE && statusCode >= HTTP_OK;
    }

}
