package no.unit.nva.cristin.projects;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FetchCristinProjectsTest {

    private static final String CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE = "/cristinQueryProjectsResponse.json";
    private static final String CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE = "/cristinGetProjectResponse.json";

    private static final String QUERY_PARAM_LANGUAGE_NB = "nb";
    private static final String QUERY_PARAM_TITLE_REINDEER = "reindeer";
    public static final String AN_URL = "http://iam.an.url";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    CristinApiClient mockCristinApiClient;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    private InputStreamReader mockGetResponseReader() {
        InputStream getResultAsStream = FetchCristinProjectsTest.class
                .getResourceAsStream(CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE);
        return new InputStreamReader(getResultAsStream);
    }

    private InputStreamReader mockQueryResponseReader() {
        InputStream queryResultsAsStream = FetchCristinProjectsTest.class
                .getResourceAsStream(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE);
        return new InputStreamReader(queryResultsAsStream);
    }

    @Test
    public void testSuccessfulResponse() throws Exception {
        when(mockCristinApiClient.fetchQueryResults(any())).thenReturn(mockQueryResponseReader());
        when(mockCristinApiClient.fetchGetResult(any())).thenAnswer(i -> mockGetResponseReader());
        when(mockCristinApiClient.queryProjects(any())).thenCallRealMethod();
        when(mockCristinApiClient.getProject(any(), any())).thenCallRealMethod();
        when(mockCristinApiClient.generateQueryProjectsUrl(any())).thenCallRealMethod();
        when(mockCristinApiClient.generateGetProjectUrl(any(), any())).thenCallRealMethod();

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("title", QUERY_PARAM_TITLE_REINDEER);
        queryParams.put("language", QUERY_PARAM_LANGUAGE_NB);
        event.put("queryStringParameters", queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects();
        mockFetchCristinProjects.setCristinApiClient(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
    }

    @Test
    public void testExceptionGettingProject() throws Exception {
        when(mockCristinApiClient.fetchQueryResults(any())).thenReturn(mockQueryResponseReader());
        when(mockCristinApiClient.queryProjects(any())).thenCallRealMethod();
        when(mockCristinApiClient.getProject(any(), any())).thenThrow(new IOException());
        when(mockCristinApiClient.generateQueryProjectsUrl(any())).thenCallRealMethod();

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("title", QUERY_PARAM_TITLE_REINDEER);
        queryParams.put("language", QUERY_PARAM_LANGUAGE_NB);
        event.put("queryStringParameters", queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
    }

    @Test
    public void testErrorResponse() throws Exception {
        when(mockCristinApiClient.queryProjects(any())).thenThrow(new IOException("Mock exception"));

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("title", QUERY_PARAM_TITLE_REINDEER);
        queryParams.put("language", QUERY_PARAM_LANGUAGE_NB);
        event.put("queryStringParameters", queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
    }

    @Test
    public void testMissingTitleParam() {

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("language", QUERY_PARAM_LANGUAGE_NB);
        event.put("queryStringParameters", queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
    }

    @Test
    public void testMissingLanguageParam() throws Exception {
        when(mockCristinApiClient.fetchQueryResults(any())).thenReturn(mockQueryResponseReader());
        when(mockCristinApiClient.fetchGetResult(any())).thenAnswer(i -> mockGetResponseReader());
        when(mockCristinApiClient.queryProjects(any())).thenCallRealMethod();
        when(mockCristinApiClient.getProject(any(), any())).thenCallRealMethod();
        when(mockCristinApiClient.generateQueryProjectsUrl(any())).thenCallRealMethod();
        when(mockCristinApiClient.generateGetProjectUrl(any(), any())).thenCallRealMethod();

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("title", QUERY_PARAM_TITLE_REINDEER);
        event.put("queryStringParameters", queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
    }

    @Test
    public void testEmptyTitleParam() {

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("title", "");
        event.put("queryStringParameters", queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        assertEquals(response.getBody(), "{\"error\":\"Parameter 'title' is mandatory\"}");
    }


    @Test
    public void testIllegalCharactersTitleParam() {

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("title", "abc123- ?");
        event.put("queryStringParameters", queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        assertEquals(response.getBody(), "{\"error\":\"Parameter 'title' may only contain alphanumeric "
                + "characters, dash and whitespace\"}");
    }

    @Test
    public void testInvalidLanguageParam() {

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("title", QUERY_PARAM_TITLE_REINDEER);
        queryParams.put("language", "invalid");
        event.put("queryStringParameters", queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        assertEquals(response.getBody(), "{\"error\":\"Parameter 'language' has invalid value\"}");
    }

    @Test
    public void testCristinQueryProjectsConnection() throws IOException {
        CristinApiClient cristinApiClient = new CristinApiClient();
        URL invalidUrl = Paths.get("/dev/null").toUri().toURL();
        cristinApiClient.fetchQueryResults(invalidUrl);
    }

    @Test
    public void testCristinGetProjectConnection() throws IOException {
        CristinApiClient cristinApiClient = new CristinApiClient();
        URL invalidUrl = Paths.get("/dev/null").toUri().toURL();
        cristinApiClient.fetchGetResult(invalidUrl);

    }

    @Test
    public void testCristinGenerateQueryProjectsUrlFromNull() throws IOException, URISyntaxException {
        CristinApiClient cristinApiClient = new CristinApiClient();
        cristinApiClient.generateQueryProjectsUrl(null);
    }


    @Test(expected = IOException.class)
    public void testExceptionOnInvalidJson() throws IOException {
        CristinApiClient cristinApiClient = new CristinApiClient();
        String invalidJson = "asdf";
        InputStream inputStream = new ByteArrayInputStream(invalidJson.getBytes(Charset.forName("UTF-8")));
        InputStreamReader reader = new InputStreamReader(inputStream);
        cristinApiClient.fromJson(reader, Project.class);
        fail();
    }

}
