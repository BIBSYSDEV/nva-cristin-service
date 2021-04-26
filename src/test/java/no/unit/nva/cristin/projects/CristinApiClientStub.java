package no.unit.nva.cristin.projects;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpResponse;
import nva.commons.core.ioutils.IoUtils;

public class CristinApiClientStub extends CristinApiClient {

    private static final String CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE = "cristinQueryProjectsResponse.json";
    private static final String CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE = "cristinGetProjectResponse.json";

    @Override
    protected long calculateProcessingTime(long startRequestTime, long endRequestTime) {
        return 1000;
    }

    @Override
    protected HttpResponse<InputStream> fetchQueryResults(URI uri) {
        return mockQueryResponse();
    }

    @Override
    protected HttpResponse<InputStream> fetchGetResult(URI uri) {
        return mockGetResponse();
    }

    private HttpResponse<InputStream> mockGetResponse() {
        var stream = IoUtils.inputStreamFromResources(CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE);
        return new HttpResponseStub(stream);
    }

    private HttpResponse<InputStream> mockQueryResponse() {
        var stream = IoUtils.inputStreamFromResources(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE);
        return new HttpResponseStub(stream);
    }
}
