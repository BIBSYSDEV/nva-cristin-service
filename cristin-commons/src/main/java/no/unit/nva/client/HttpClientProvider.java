package no.unit.nva.client;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.time.Duration;

public class HttpClientProvider {

  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
  private static final HttpClient DEFAULT_HTTP_CLIENT = buildDefaultHttpClient();

  /** Shared default HttpClient without authentication. Must not be closed by callers. */
  public static HttpClient defaultHttpClient() {
    return DEFAULT_HTTP_CLIENT;
  }

  public static HttpClient.Builder defaultHttpClientBuilder() {
    return HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .version(Version.HTTP_1_1)
        .connectTimeout(CONNECT_TIMEOUT);
  }

  private static HttpClient buildDefaultHttpClient() {
    return defaultHttpClientBuilder().build();
  }
}
