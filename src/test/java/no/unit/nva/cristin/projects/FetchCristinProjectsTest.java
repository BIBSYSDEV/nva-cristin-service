package no.unit.nva.cristin.projects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class FetchCristinProjectsTest {

    private static final String CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE = "/cristinQueryProjectsResponse.json";
    private static final String CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE = "/cristinGetProjectResponse.json";
    private static final String QUERY_STRING_PARAMETERS_KEY = "queryStringParameters";
    private static final String TITLE_KEY = "title";
    private static final String LANGUAGE_KEY = "language";
    private static final String EMPTY_STRING = "";
    private static final String LANGUAGE_NB = "nb";
    private static final String LANGUAGE_INVALID = "invalid";
    private static final String TITLE_REINDEER = "reindeer";
    private static final String TITLE_ILLEGAL_CHARACTERS = "abc123- ,-?";
    private static final String INVALID_JSON = "This is not valid JSON!";
    private static final String MOCK_EXCEPTION = "Mock exception";

    CristinApiClient mockCristinApiClient;

    @BeforeEach
    public void setUp() {
        mockCristinApiClient = mock(CristinApiClient.class);
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
    public void handlerReturnsOkWhenInputContainsTitleAndLanguage() throws Exception {
        when(mockCristinApiClient.fetchQueryResults(any())).thenReturn(mockQueryResponseReader());
        when(mockCristinApiClient.fetchGetResult(any())).thenAnswer(i -> mockGetResponseReader());
        when(mockCristinApiClient.queryAndEnrichProjects(any(),any())).thenCallRealMethod();
        when(mockCristinApiClient.queryProjects(any())).thenCallRealMethod();
        when(mockCristinApiClient.getProject(any(), any())).thenCallRealMethod();
        when(mockCristinApiClient.generateQueryProjectsUrl(any())).thenCallRealMethod();
        when(mockCristinApiClient.generateGetProjectUrl(any(), any())).thenCallRealMethod();

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(TITLE_KEY, TITLE_REINDEER);
        queryParams.put(LANGUAGE_KEY, LANGUAGE_NB);
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects();
        mockFetchCristinProjects.setCristinApiClient(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
    }

    @Test
    public void testExceptionGettingProject() throws Exception {
        when(mockCristinApiClient.fetchQueryResults(any())).thenReturn(mockQueryResponseReader());
        when(mockCristinApiClient.queryAndEnrichProjects(any(), any())).thenCallRealMethod();
        when(mockCristinApiClient.queryProjects(any())).thenCallRealMethod();
        when(mockCristinApiClient.getProject(any(), any())).thenThrow(new IOException());
        when(mockCristinApiClient.generateQueryProjectsUrl(any())).thenCallRealMethod();

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(TITLE_KEY, TITLE_REINDEER);
        queryParams.put(LANGUAGE_KEY, LANGUAGE_NB);
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
    }

    @Test
    public void testErrorResponse() throws Exception {
        when(mockCristinApiClient.queryAndEnrichProjects(any(), any())).thenThrow(new IOException(MOCK_EXCEPTION));

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(TITLE_KEY, TITLE_REINDEER);
        queryParams.put(LANGUAGE_KEY, LANGUAGE_NB);
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
    }

    @Test
    public void testMissingTitleParam() {

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(LANGUAGE_KEY, LANGUAGE_NB);
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
    }

    @Test
    public void testMissingLanguageParam() throws Exception {
        when(mockCristinApiClient.fetchQueryResults(any())).thenReturn(mockQueryResponseReader());
        when(mockCristinApiClient.fetchGetResult(any())).thenAnswer(i -> mockGetResponseReader());
        when(mockCristinApiClient.queryAndEnrichProjects(any(), any())).thenCallRealMethod();
        when(mockCristinApiClient.queryProjects(any())).thenCallRealMethod();
        when(mockCristinApiClient.getProject(any(), any())).thenCallRealMethod();
        when(mockCristinApiClient.generateQueryProjectsUrl(any())).thenCallRealMethod();
        when(mockCristinApiClient.generateGetProjectUrl(any(), any())).thenCallRealMethod();

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(TITLE_KEY, TITLE_REINDEER);
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
    }

    @Test
    public void testEmptyTitleParam() {

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(TITLE_KEY, EMPTY_STRING);
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        assertTrue(response.getBody().contains(FetchCristinProjects.TITLE_IS_NULL));
    }


    @Test
    public void testIllegalCharactersTitleParam() {

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(TITLE_KEY, TITLE_ILLEGAL_CHARACTERS);
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        assertTrue(response.getBody().contains(FetchCristinProjects.TITLE_ILLEGAL_CHARACTERS));
    }

    @Test
    public void testInvalidLanguageParam() {

        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put(TITLE_KEY, TITLE_REINDEER);
        queryParams.put(LANGUAGE_KEY, LANGUAGE_INVALID);
        event.put(QUERY_STRING_PARAMETERS_KEY, queryParams);

        FetchCristinProjects mockFetchCristinProjects = new FetchCristinProjects(mockCristinApiClient);
        GatewayResponse response = mockFetchCristinProjects.handleRequest(event, null);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
        assertEquals(response.getHeaders().get(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
        assertTrue(response.getBody().contains(FetchCristinProjects.LANGUAGE_INVALID));
    }


    @Test
    public void testCristinGenerateQueryProjectsUrlFromNull() throws IOException, URISyntaxException {
        CristinApiClient cristinApiClient = new CristinApiClient();
        cristinApiClient.generateQueryProjectsUrl(null);
    }


    @Test
    public void testExceptionOnInvalidJson() {
        InputStream inputStream = new ByteArrayInputStream(INVALID_JSON.getBytes(StandardCharsets.UTF_8));
        InputStreamReader reader = new InputStreamReader(inputStream);
        Executable action = () -> CristinApiClient.fromJson(reader, Project.class);
        assertThrows(IOException.class, action);
    }

}
