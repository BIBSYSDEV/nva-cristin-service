package no.unit.nva.client;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.time.Duration;

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

}
