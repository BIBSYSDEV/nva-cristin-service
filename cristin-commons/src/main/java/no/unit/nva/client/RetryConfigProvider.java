package no.unit.nva.client;

import static java.util.Objects.isNull;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class RetryConfigProvider {

    /**
     * Creates an object that helps in retrying failed http requests. Can be used when calling upstream.
     */
    public static RetryRegistry defaultRetryRegistry() {
        return RetryRegistry.of(defaultRetryConfigFactory());
    }

    /**
     * Creates an object that helps in retrying failed async http requests. Can be used when calling upstream.
     */
    public static RetryRegistry defaultRetryRegistryAsync() {
        return RetryRegistry.of(defaultRetryConfigFactoryAsync());
    }

    private static RetryConfig defaultRetryConfigFactory() {
        return RetryConfig.<HttpResponse>custom()
                   .maxAttempts(3)
                   .waitDuration(Duration.ofMillis(100))
                   .retryOnResult(resultStatusIsServerError())
                   .retryExceptions(Exception.class)
                   .build();
    }

    private static Predicate<HttpResponse> resultStatusIsServerError() {
        return httpResponse -> isNull(httpResponse)
                               || httpResponse.statusCode() >= 500 && httpResponse.statusCode() <= 599;
    }

    private static RetryConfig defaultRetryConfigFactoryAsync() {
        return RetryConfig.<CompletableFuture<HttpResponse>>custom()
                   .maxAttempts(3)
                   .waitDuration(Duration.ofMillis(100))
                   .retryOnResult(resultStatusIsServerErrorAsync())
                   .retryExceptions(Exception.class)
                   .build();
    }

    private static Predicate<CompletableFuture<HttpResponse>> resultStatusIsServerErrorAsync() {
        return httpResponse -> {
            try {
                return isNull(httpResponse)
                       || httpResponse.get().statusCode() >= 500 && httpResponse.get().statusCode() <= 599;
            } catch (Exception e) {
                return true;
            }
        };
    }

}
