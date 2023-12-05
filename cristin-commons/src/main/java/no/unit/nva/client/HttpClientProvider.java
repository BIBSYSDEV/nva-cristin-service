package no.unit.nva.client;

import static java.util.Objects.isNull;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Predicate;

public class HttpClientProvider {

    /**
     * Default HttpClient without authentication for general use.
    **/
    public static HttpClient defaultHttpClient() {
        return HttpClient.newBuilder()
                   .followRedirects(HttpClient.Redirect.ALWAYS)
                   .version(Version.HTTP_1_1)
                   .connectTimeout(Duration.ofSeconds(15))
                   .build();
    }

    /**
     * Creates an object that helps in retrying failed http requests. Can be used when calling upstream.
     */
    public static RetryRegistry defaultRetryRegistry() {
        return RetryRegistry.of(defaultRetryConfigFactory());
    }

    @SuppressWarnings("rawtypes")
    private static RetryConfig defaultRetryConfigFactory() {
        return RetryConfig.<HttpResponse>custom()
                   .maxAttempts(3)
                   .waitDuration(Duration.ofMillis(100))
                   .retryOnResult(resultStatusIsServerError())
                   .retryExceptions(Exception.class)
                   .build();
    }

    @SuppressWarnings("rawtypes")
    private static Predicate<HttpResponse> resultStatusIsServerError() {
        return httpResponse -> isNull(httpResponse)
                               || httpResponse.statusCode() >= 500 && httpResponse.statusCode() <= 599;
    }

}
