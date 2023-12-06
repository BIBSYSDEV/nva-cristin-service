package no.unit.nva.cristin.common.client;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import no.unit.nva.exception.FailedHttpRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApiClientTest {

    public static final String EMPTY_ARRAY = "[]";

    private ApiClient apiClient;
    private HttpClient httpClient;
    private HttpRequest httpRequest;
    private HttpResponse<String> okResponse;
    private HttpResponse<String> badResponse;

    @BeforeEach
    void setup() {
        httpClient = mock(HttpClient.class);
        apiClient = new ApiClient(httpClient);
        httpRequest = mock(HttpRequest.class);
        doReturn(randomUri()).when(httpRequest).uri();
        okResponse = createOkResponse();
        badResponse = createBadResponse();
    }

    @Test
    void shouldTryRetrievingResponseSeveralTimesBeforeSucceeding() throws Exception {
        mockResponseWithErrorStatusCodeTheFirstTwoTimesButReturnsSuccessStatusCodeTheLastTime();
        var response = apiClient.getSuccessfulResponseOrThrowException(httpRequest);

        verify(httpClient, times(3)).send(any(), any());
        assertThat(response.body(),  equalTo(EMPTY_ARRAY));
    }

    @Test
    void shouldReturnServerErrorStatusCodeWhenAllRetriesReturnsServerError() throws Exception {
        mockResponseWithOnlyErrorStatusCodeOnAllAttempts();
        var response = apiClient.getSuccessfulResponseOrThrowException(httpRequest);

        verify(httpClient, times(3)).send(any(), any());
        assertThat(response.statusCode(), equalTo(HttpURLConnection.HTTP_INTERNAL_ERROR));
    }

    @Test
    void shouldThrowsSomeExceptionsBeforeSucceeding() throws Exception {
        mockResponseThatThrowsExceptionOnTheFirstAttemptsButThenSucceeds();
        var response = apiClient.getSuccessfulResponseOrThrowException(httpRequest);

        verify(httpClient, times(3)).send(any(), any());
        assertThat(response.body(),  equalTo(EMPTY_ARRAY));
    }

    @Test
    void shouldThrowCorrectExceptionWhenAllAttemptsFail() throws Exception {
        mockResponseThatThrowsExceptionOnAllRequests();

        assertThrows(FailedHttpRequestException.class,
                     () -> apiClient.getSuccessfulResponseOrThrowException(httpRequest));
        verify(httpClient, times(3)).send(any(), any());
    }

    @Test
    void shouldTryRetrievingResponseSeveralTimesBeforeSucceedingAsync() throws Exception {
        mockResponseWithErrorStatusCodeTheFirstTwoTimesButReturnsSuccessStatusCodeTheLastTimeAsync();
        var response = apiClient.fetchGetResultAsync(randomUri()).get();

        verify(httpClient, times(3)).sendAsync(any(), any());
        assertThat(response.body(),  equalTo(EMPTY_ARRAY));
    }

    @Test
    void shouldReturnServerErrorStatusCodeWhenAllRetriesReturnsServerErrorAsync() throws Exception {
        mockResponseWithOnlyErrorStatusCodeOnAllAttemptsAsync();
        var response = apiClient.fetchGetResultAsync(randomUri()).get();

        verify(httpClient, times(3)).sendAsync(any(), any());
        assertThat(response.statusCode(), equalTo(HttpURLConnection.HTTP_INTERNAL_ERROR));
    }

    @Test
    void shouldThrowsSomeExceptionsBeforeSucceedingAsync() throws Exception {
        mockResponseThatThrowsExceptionOnTheFirstAttemptsButThenSucceedsAsync();
        var response = apiClient.fetchGetResultAsync(randomUri()).get();

        verify(httpClient, times(3)).sendAsync(any(), any());
        assertThat(response.body(),  equalTo(EMPTY_ARRAY));
    }

    @Test
    void shouldThrowCorrectExceptionWhenAllAttemptsFailAsync() {
        mockResponseThatThrowsExceptionOnAllRequestsAsync();

        assertThrows(RuntimeException.class,
                     () -> apiClient.fetchGetResultAsync(randomUri()).get());
        verify(httpClient, times(3)).sendAsync(any(), any());
    }


    private void mockResponseWithErrorStatusCodeTheFirstTwoTimesButReturnsSuccessStatusCodeTheLastTime()
        throws IOException, InterruptedException {

        doReturn(badResponse, badResponse, okResponse)
            .when(httpClient).send(any(),any());

        apiClient = new ApiClient(httpClient);
    }

    private void mockResponseWithOnlyErrorStatusCodeOnAllAttempts() throws IOException, InterruptedException {
        doReturn(badResponse, badResponse, badResponse)
            .when(httpClient).send(any(),any());

        apiClient = new ApiClient(httpClient);
    }

    private void mockResponseThatThrowsExceptionOnTheFirstAttemptsButThenSucceeds()
        throws IOException, InterruptedException {

        doThrow(ioException()).doThrow(timeoutException()).doReturn(okResponse)
            .when(httpClient)
            .send(any(), any());

        apiClient = new ApiClient(httpClient);
    }

    private void mockResponseThatThrowsExceptionOnAllRequests() throws IOException, InterruptedException {
        doThrow(ioException()).doThrow(timeoutException()).doThrow(interruptedException())
            .when(httpClient)
            .send(any(), any());

        apiClient = new ApiClient(httpClient);
    }

    private void mockResponseWithErrorStatusCodeTheFirstTwoTimesButReturnsSuccessStatusCodeTheLastTimeAsync() {
        var badResponseAsync = completedFuture(createBadResponse());
        var okResponseAsync = completedFuture(createOkResponse());

        doReturn(badResponseAsync, badResponseAsync, okResponseAsync)
            .when(httpClient).sendAsync(any(),any());

        apiClient = new ApiClient(httpClient);
    }

    private void mockResponseWithOnlyErrorStatusCodeOnAllAttemptsAsync() {
        var badResponseAsync = completedFuture(createBadResponse());

        doReturn(badResponseAsync, badResponseAsync, badResponseAsync)
            .when(httpClient).sendAsync(any(),any());

        apiClient = new ApiClient(httpClient);
    }

    private void mockResponseThatThrowsExceptionOnTheFirstAttemptsButThenSucceedsAsync() {
        var okResponseAsync = completedFuture(createOkResponse());
        var uncheckedException = new RuntimeException();

        doThrow(uncheckedException).doThrow(uncheckedException).doReturn(okResponseAsync)
            .when(httpClient)
            .sendAsync(any(), any());

        apiClient = new ApiClient(httpClient);
    }

    private void mockResponseThatThrowsExceptionOnAllRequestsAsync() {
        var uncheckedException = new RuntimeException();

        doThrow(uncheckedException).doThrow(uncheckedException).doThrow(uncheckedException)
            .when(httpClient)
            .sendAsync(any(), any());

        apiClient = new ApiClient(httpClient);
    }


    @SuppressWarnings("unchecked")
    private HttpResponse<String> createOkResponse() {
        var response = (HttpResponse<String>) mock(HttpResponse.class);
        doReturn(HttpURLConnection.HTTP_OK).when(response).statusCode();
        doReturn(EMPTY_ARRAY).when(response).body();
        return response;
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> createBadResponse() {
        var response = (HttpResponse<String>) mock(HttpResponse.class);
        doReturn(HttpURLConnection.HTTP_INTERNAL_ERROR).when(response).statusCode();
        return response;
    }

    private HttpTimeoutException timeoutException() {
        return new HttpTimeoutException(randomString());
    }

    private IOException ioException() {
        return new IOException(randomString());
    }

    private InterruptedException interruptedException() {
        return new InterruptedException(randomString());
    }

}
