package no.unit.nva.utils;

import static java.util.Objects.nonNull;
import static nva.commons.core.attempt.Try.attempt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import org.mockito.ArgumentMatcher;

public class FakeCognitoHttpClient extends HttpClient {

    private final HttpClient client;

    private FakeCognitoHttpClient(HttpClient client) {
        this.client = client;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return client.cookieHandler();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return client.connectTimeout();
    }

    @Override
    public Redirect followRedirects() {
        return client.followRedirects();
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return client.proxy();
    }

    @Override
    public SSLContext sslContext() {
        return client.sslContext();
    }

    @Override
    public SSLParameters sslParameters() {
        return client.sslParameters();
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return client.authenticator();
    }

    @Override
    public Version version() {
        return client.version();
    }

    @Override
    public Optional<Executor> executor() {
        return client.executor();
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, BodyHandler<T> responseBodyHandler)
        throws IOException, InterruptedException {
        return client.send(request, responseBodyHandler);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler) {
        return client.sendAsync(request, responseBodyHandler);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, BodyHandler<T> responseBodyHandler,
                                                            PushPromiseHandler<T> pushPromiseHandler) {
        return client.sendAsync(request, responseBodyHandler, pushPromiseHandler);
    }

    public static class Builder {
        private final HttpClient client = mock(HttpClient.class);

        public Builder withTokenEndpoint() {
            var mockTokenResponse = mock(HttpResponse.class);
            when(mockTokenResponse.statusCode()).thenReturn(200);
            when(mockTokenResponse.body()).thenReturn("{\"access_token\": \"test_token\"}");
            attempt(() -> when(client.send(argThat(new TokenResponseMatcher()),
                                          eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockTokenResponse)).orElseThrow();
            return this;
        }

        public Builder withAuthorizationCodeEndpoint() {
            var mockCodeResponse = mock(HttpResponse.class);
            when(mockCodeResponse.statusCode()).thenReturn(302);
            HttpHeaders headers = HttpHeaders.of(
                Map.of("Location", List.of("http://localhost:3000?code=test_code")), (a, b) -> true);
            when(mockCodeResponse.headers()).thenReturn(headers);
            attempt(() -> when(client.send(argThat(new CodeResponseMatcher()), eq(HttpResponse.BodyHandlers.ofString())))
                .thenReturn(mockCodeResponse)).orElseThrow();
            return this;
        }

        public FakeCognitoHttpClient build() {
            return new FakeCognitoHttpClient(client);
        }
    }

    static class CodeResponseMatcher implements ArgumentMatcher<HttpRequest> {

        @Override
        public boolean matches(HttpRequest request) {
            if (nonNull(request)) {
                return request.uri().getPath().contains("/login") && request.method().equals("POST");
            }
            return false;
        }
    }

    static class TokenResponseMatcher implements ArgumentMatcher<HttpRequest> {

        @Override
        public boolean matches(HttpRequest request) {
            if (nonNull(request)) {
                return request.uri().getPath().contains("/oauth2/token") && request.method().equals("POST");
            }
            return false;
        }
    }
}
