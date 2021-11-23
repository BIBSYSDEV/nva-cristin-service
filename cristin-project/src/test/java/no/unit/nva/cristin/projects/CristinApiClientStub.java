package no.unit.nva.cristin.projects;

import nva.commons.core.ioutils.IoUtils;

import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class CristinApiClientStub extends CristinApiClient {

    public static final String CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE = "cristinGetProjectResponse.json";
    public static final String CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE = "cristinQueryProjectsResponse.json";
    private final String responseBody;

    public CristinApiClientStub() {
        this(IoUtils.stringFromResources(Path.of(CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE)));
    }

    public CristinApiClientStub(String sampleResponse) {
        responseBody = sampleResponse;
    }

    @Override
    public long calculateProcessingTime(long startRequestTime, long endRequestTime) {
        return 1000;
    }

    @Override
    protected HttpResponse<String> fetchGetResult(URI uri) {
        return mockGetResponse();
    }

    @Override
    protected CompletableFuture<HttpResponse<String>> fetchGetResultAsync(URI uri) {
        return CompletableFuture.completedFuture(mockGetResponse());
    }

    @Override
    protected HttpResponse<String> fetchQueryResults(URI uri) {
        return mockQueryResponse();
    }

    private HttpResponse<String> mockGetResponse() {
        return new HttpResponseStub(responseBody);
    }

    private HttpResponse<String> mockQueryResponse() {
        String body = IoUtils.stringFromResources(Path.of(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE));
        return new HttpResponseStub(body);
    }
}
