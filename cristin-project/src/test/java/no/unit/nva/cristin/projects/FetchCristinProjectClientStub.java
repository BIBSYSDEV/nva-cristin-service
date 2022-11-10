package no.unit.nva.cristin.projects;

import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import nva.commons.core.ioutils.IoUtils;

public class FetchCristinProjectClientStub extends FetchCristinProjectApiClient {

    public static final String CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE = "cristinGetProjectResponse.json";
    public static final String CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE = "cristinQueryProjectsResponse.json";
    private final String responseBody;

    public FetchCristinProjectClientStub() {
        this(IoUtils.stringFromResources(Path.of(CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE)));
    }

    public FetchCristinProjectClientStub(String sampleResponse) {
        responseBody = sampleResponse;
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
        return new HttpResponseFaker(responseBody);
    }

    private HttpResponse<String> mockQueryResponse() {
        var body = IoUtils.stringFromResources(Path.of(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE));
        return new HttpResponseFaker(body);
    }

}
