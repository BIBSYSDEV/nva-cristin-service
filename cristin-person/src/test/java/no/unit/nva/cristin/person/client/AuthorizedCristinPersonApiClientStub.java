package no.unit.nva.cristin.person.client;

import no.unit.nva.cristin.testing.HttpResponseFaker;
import nva.commons.core.ioutils.IoUtils;

import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class AuthorizedCristinPersonApiClientStub extends AuthorizedCristinPersonApiClient {

    public static final String CRISTIN_GET_PERSON_RESPONSE_JSON_FILE = "cristinGetPersonResponse.json";
    public static final String CRISTIN_QUERY_PERSONS_RESPONSE_JSON_FILE = "cristinQueryPersonResponse.json";
    private final String getResponseBody;
    private final String queryResponseBody;

    public AuthorizedCristinPersonApiClientStub() {
        this(IoUtils.stringFromResources(Path.of(CRISTIN_GET_PERSON_RESPONSE_JSON_FILE)),
            IoUtils.stringFromResources(Path.of(CRISTIN_QUERY_PERSONS_RESPONSE_JSON_FILE)));
    }

    public AuthorizedCristinPersonApiClientStub(String getResponseBody, String queryResponseBody) {
        this.getResponseBody = getResponseBody;
        this.queryResponseBody = queryResponseBody;
    }

    @Override
    public long calculateProcessingTime(long startRequestTime, long endRequestTime) {
        return 1000;
    }

    @Override
    public HttpResponse<String> fetchGetResult(URI uri) {
        return mockGetResponse();
    }

    @Override
    public CompletableFuture<HttpResponse<String>> fetchGetResultAsync(URI uri) {
        return CompletableFuture.completedFuture(mockGetResponse());
    }

    @Override
    public HttpResponse<String> fetchQueryResults(URI uri) {
        return mockQueryResponse();
    }

    private HttpResponse<String> mockGetResponse() {
        return new HttpResponseFaker(getResponseBody);
    }

    private HttpResponse<String> mockQueryResponse() {
        return new HttpResponseFaker(queryResponseBody);
    }
}
