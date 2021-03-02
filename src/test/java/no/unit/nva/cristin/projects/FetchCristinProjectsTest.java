package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.JsonUtils;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static no.unit.nva.cristin.projects.FetchCristinProjects.LANGUAGE_QUERY_PARAMETER;
import static no.unit.nva.cristin.projects.FetchCristinProjects.TITLE_QUERY_PARAMETER;
import static nva.commons.apigateway.ApiGatewayHandler.APPLICATION_PROBLEM_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FetchCristinProjectsTest {

    private static final String CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE = "cristinQueryProjectsResponse.json";
    private static final String CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE = "cristinGetProjectResponse.json";
    private static final String LANGUAGE_KEY = "language";
    private static final String EMPTY_STRING = "";
    private static final String LANGUAGE_NB = "nb";
    private static final String LANGUAGE_INVALID = "invalid";
    private static final String TITLE_REINDEER = "reindeer";
    private static final String TITLE_ILLEGAL_CHARACTERS = "abc123- ,-?";
    private static final String INVALID_JSON = "This is not valid JSON!";
    private static final String MOCK_EXCEPTION = "Mock exception";

    CristinApiClient mockCristinApiClient;

    private static final String ALLOW_ALL_ORIGIN = "*";
    private Context context;
    private ByteArrayOutputStream output;
    private FetchCristinProjects handler;

    @BeforeEach
    void setUp() {
        mockCristinApiClient = mock(CristinApiClient.class);
        Environment environment = mock(Environment.class);
        when(environment.readEnv(anyString())).thenReturn(ALLOW_ALL_ORIGIN);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new FetchCristinProjects(mockCristinApiClient, environment);
    }

    @ParameterizedTest
    @ArgumentsSource(TestPairProvider.class)
    void handlerReturnsExpectedBodyWhenRequestInputIsValid(String queryResponse,
                                                           String getResponse,
                                                           String expected) throws IOException, URISyntaxException {
        initDefaultCristinApiClientMocks(queryResponse, getResponse);
        var actual = sendDefaultQuery().getBody();
        assertEquals(expected, actual);
    }

    @Test
    public void handlerReturnsOkWhenInputContainsTitleAndLanguage() throws Exception {
        initDefaultCristinApiClientMocks();
        GatewayResponse<ProjectPresentation[]> response = sendDefaultQuery();
        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
    }

    @Test
    public void handlerIgnoresErrorsWhenTryingToEnrichProjectInformation() throws Exception {
        cristinClientThrowingIoExceptionWhenFetchingProjectInfo();

        GatewayResponse<ProjectPresentation[]> response = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    public void handlerThrowsInternalErrorWhenQueryingProjectsFails() throws Exception {
        when(mockCristinApiClient.queryAndEnrichProjects(any(), any())).thenThrow(new IOException(MOCK_EXCEPTION));

        GatewayResponse<ProjectPresentation[]> response = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    public void handlerThrowsBadRequestWhenMissingTitleQueryParameter() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(LANGUAGE_QUERY_PARAMETER, LANGUAGE_NB));

        handler.handleRequest(input, output, context);
        GatewayResponse<ProjectPresentation[]> response = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    public void handlerSetsDefaultValueForMissingOptionalLanguageParameterAndReturnOk() throws Exception {
        initDefaultCristinApiClientMocks();

        InputStream input = requestWithQueryParameters(Map.of(TITLE_QUERY_PARAMETER, TITLE_REINDEER));

        handler.handleRequest(input, output, context);
        GatewayResponse<ProjectPresentation[]> response = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    public void handlerReceivesAllowOriginHeaderValueFromEnvironmentAndPutsItOnResponse() throws Exception {
        initDefaultCristinApiClientMocks();

        GatewayResponse<ProjectPresentation[]> response = sendDefaultQuery();
        assertEquals(ALLOW_ALL_ORIGIN, response.getHeaders().get(ApiGatewayHandler.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    public void handlerReturnsBadRequestWhenTitleQueryParamIsEmpty() throws Exception {

        initDefaultCristinApiClientMocks();

        InputStream input = requestWithQueryParameters(Map.of(TITLE_QUERY_PARAMETER, EMPTY_STRING));

        handler.handleRequest(input, output, context);
        GatewayResponse<ProjectPresentation[]> response = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertTrue(response.getBody().contains(FetchCristinProjects.TITLE_MISSING_OR_HAS_ILLEGAL_CHARACTERS));
    }

    @Test
    public void handlerReturnsBadRequestWhenReceivingTitleQueryParamWithIllegalCharacters() throws Exception {
        initDefaultCristinApiClientMocks();

        InputStream input = requestWithQueryParameters(Map.of(TITLE_QUERY_PARAMETER, TITLE_ILLEGAL_CHARACTERS));

        handler.handleRequest(input, output, context);
        GatewayResponse<ProjectPresentation[]> response = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertTrue(response.getBody().contains(FetchCristinProjects.TITLE_MISSING_OR_HAS_ILLEGAL_CHARACTERS));
    }

    @Test
    public void handlerReturnsBadRequestWhenReceivingInvalidLanguageQueryParam() throws Exception {
        initDefaultCristinApiClientMocks();

        InputStream input = requestWithQueryParameters(Map.of(TITLE_QUERY_PARAMETER, TITLE_REINDEER,
            LANGUAGE_KEY, LANGUAGE_INVALID));

        handler.handleRequest(input, output, context);
        GatewayResponse<ProjectPresentation[]> response = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertTrue(response.getBody().contains(FetchCristinProjects.LANGUAGE_INVALID));
    }

    @Test
    public void cristinApiClientWillStillGenerateQueryProjectsUrlEvenWithoutParameters()
        throws IOException, URISyntaxException {
        CristinApiClient cristinApiClient = new CristinApiClient();
        cristinApiClient.generateQueryProjectsUrl(null);
    }

    @Test
    public void readerThrowsIoExceptionWhenReadingInvalidJson() {
        InputStream inputStream = new ByteArrayInputStream(INVALID_JSON.getBytes(StandardCharsets.UTF_8));
        InputStreamReader reader = new InputStreamReader(inputStream);
        Executable action = () -> CristinApiClient.fromJson(reader, Project.class);
        assertThrows(IOException.class, action);
    }

    private void cristinClientThrowingIoExceptionWhenFetchingProjectInfo() throws IOException, URISyntaxException {
        when(mockCristinApiClient.fetchQueryResults(any())).thenReturn(mockQueryResponseReader());
        when(mockCristinApiClient.queryAndEnrichProjects(any(), any())).thenCallRealMethod();
        when(mockCristinApiClient.queryProjects(any())).thenCallRealMethod();
        when(mockCristinApiClient.generateQueryProjectsUrl(any())).thenCallRealMethod();

        when(mockCristinApiClient.getProject(any(), any())).thenThrow(new IOException());
    }

    private GatewayResponse<ProjectPresentation[]> sendDefaultQuery() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(TITLE_QUERY_PARAMETER, TITLE_REINDEER,
            LANGUAGE_QUERY_PARAMETER, LANGUAGE_NB));
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private void initDefaultCristinApiClientMocks() throws IOException, URISyntaxException {
        initDefaultCristinApiClientMocks(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE, CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE);
    }

    private void initDefaultCristinApiClientMocks(String queryData, String getData) throws
            IOException, URISyntaxException {
        when(mockCristinApiClient.fetchQueryResults(any())).thenReturn(getReader(queryData));
        when(mockCristinApiClient.fetchGetResult(any())).thenAnswer(i -> getReader(getData));
        when(mockCristinApiClient.queryAndEnrichProjects(any(), any())).thenCallRealMethod();
        when(mockCristinApiClient.queryProjects(any())).thenCallRealMethod();
        when(mockCristinApiClient.getProject(any(), any())).thenCallRealMethod();
        when(mockCristinApiClient.generateQueryProjectsUrl(any())).thenCallRealMethod();
        when(mockCristinApiClient.generateGetProjectUrl(any(), any())).thenCallRealMethod();
    }

    private InputStream requestWithQueryParameters(Map<String, String> map) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(JsonUtils.objectMapper)
            .withBody(null)
            .withQueryParameters(map)
            .build();
    }

    private InputStreamReader mockQueryResponseReader() {
        return getReader(CRISTIN_QUERY_PROJECTS_RESPONSE_JSON_FILE);
    }

    private InputStreamReader getReader(String resource) {
        InputStream queryResultsAsStream = IoUtils.inputStreamFromResources(resource);
        return new InputStreamReader(queryResultsAsStream);
    }
}
