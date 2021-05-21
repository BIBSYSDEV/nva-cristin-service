package no.unit.nva.cristin.projects;

import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import nva.commons.core.ioutils.IoUtils;

public class CristinApiClientStub extends CristinApiClient {

    protected static final String CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE = "cristinQueryProjectsResponse.json";
    private static final String CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE = "cristinGetProjectResponse.json";

    @Override
    protected long calculateProcessingTime(long startRequestTime, long endRequestTime) {
        return 1000;
    }

    @Override
    protected HttpResponse<String> fetchQueryResults(URI uri) {
        return mockQueryResponse();
    }

    @Override
    protected HttpResponse<String> fetchGetResult(URI uri) {
        return mockGetResponse();
    }

    private HttpResponse<String> mockGetResponse() {
        String body = IoUtils.stringFromResources(Path.of(CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE));
        return new HttpResponseStub(body);
    }

    private HttpResponse<String> mockQueryResponse() {
        String body = IoUtils.stringFromResources(Path.of(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE));
        return new HttpResponseStub(body);
    }
}
