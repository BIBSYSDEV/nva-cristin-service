package no.unit.nva.cristin.person.client;

import no.unit.nva.cristin.common.client.ApiClient;

import java.net.http.HttpClient;
import java.time.Duration;

public class DeletePersonEmploymentClient  extends ApiClient {
    /**
     * Create CristinPersonApiClient with default HTTP client.
     */
    public DeletePersonEmploymentClient() {
        this(HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build());
    }

    public DeletePersonEmploymentClient(HttpClient client) {
        super(client);
    }

    public Void deletePersonEmployment(String personId, String employmentId) {
        return null;
    }
}
