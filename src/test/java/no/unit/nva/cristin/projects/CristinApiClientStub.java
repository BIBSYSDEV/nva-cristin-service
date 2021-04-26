package no.unit.nva.cristin.projects;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import nva.commons.core.ioutils.IoUtils;

public class CristinApiClientStub extends CristinApiClient {

    private static final String CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE = "cristinQueryProjectsResponse.json";
    private static final String CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE = "cristinGetProjectResponse.json";

    @Override
    protected long calculateProcessingTime(long startRequestTime, long endRequestTime) {
        return 1000;
    }

    @Override
    protected InputStreamReader fetchQueryResults(URL url) {
        return mockQueryResponseReader();
    }

    @Override
    protected InputStreamReader fetchGetResult(URL url) {
        return mockGetResponseReader();
    }

    @Override
    protected HttpResponse<InputStream> fetchGetResult(URI uri) {
        return mockGetResponse();
    }

    private InputStreamReader mockGetResponseReader() {
        return new InputStreamReader(IoUtils
            .inputStreamFromResources(CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE), StandardCharsets.UTF_8);
    }

    private InputStreamReader mockQueryResponseReader() {
        return new InputStreamReader(IoUtils
            .inputStreamFromResources(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE), StandardCharsets.UTF_8);
    }

    private HttpResponse<InputStream> mockGetResponse() {
        var stream = IoUtils.inputStreamFromResources(CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE);
        return new HttpResponseStub(stream);
    }
}
