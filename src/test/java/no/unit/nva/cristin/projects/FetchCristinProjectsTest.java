package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.FetchCristinProjects.LANGUAGE_QUERY_PARAMETER;
import static no.unit.nva.cristin.projects.FetchCristinProjects.TITLE_QUERY_PARAMETER;
import static nva.commons.apigateway.ApiGatewayHandler.APPLICATION_PROBLEM_JSON;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.JsonUtils;
import nva.commons.core.ioutils.IoUtils;
import org.apache.commons.codec.Charsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class FetchCristinProjectsTest {

    private static final String LANGUAGE_KEY = "language";
    private static final String EMPTY_STRING = "";
    private static final String LANGUAGE_NB = "nb";
    private static final String LANGUAGE_INVALID = "invalid";
    private static final String TITLE_REINDEER = "reindeer";
    private static final String TITLE_ILLEGAL_CHARACTERS = "abc123- ,-?";
    private static final String INVALID_JSON = "This is not valid JSON!";

    private static final String ALLOWED_ORIGIN_ENV = "ALLOWED_ORIGIN";
    private static final String CRISTIN_API_HOST_ENV = "CRISTIN_API_HOST";
    private static final String CRISTIN_API_DUMMY_HOST = "example.com";
    private static final String ALLOW_ALL_ORIGIN = "*";
    private static final String API_RESPONSE_NON_ENRICHED_PROJECTS_JSON = "api_response_non_enriched_projects.json";
    private static final String API_RESPONSE_ONE_CRISTIN_PROJECT_TO_NVA_PROJECT_JSON =
        "api_response_one_cristin_project_to_nva_project.json";
    private static final ObjectMapper OBJECT_MAPPER = JsonUtils.objectMapper;
    private CristinApiClient cristinApiClientStub;
    private Environment environment;
    private Context context;
    private ByteArrayOutputStream output;
    private FetchCristinProjects handler;

    @BeforeEach
    void setUp() {
        environment = mock(Environment.class);
        when(environment.readEnv(ALLOWED_ORIGIN_ENV)).thenReturn(ALLOW_ALL_ORIGIN);
        when(environment.readEnv(CRISTIN_API_HOST_ENV)).thenReturn(CRISTIN_API_DUMMY_HOST);
        cristinApiClientStub = new CristinApiClientStub(environment.readEnv(CRISTIN_API_HOST_ENV));
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new FetchCristinProjects(cristinApiClientStub, environment);
    }

    @ParameterizedTest
    @ArgumentsSource(TestPairProvider.class)
    void handlerReturnsExpectedBodyWhenRequestInputIsValid(String queryResponse,
                                                           String getResponse,
                                                           String expected) throws IOException {
        cristinApiClientStub = spy(cristinApiClientStub);
        when(cristinApiClientStub.fetchQueryResults(any())).thenReturn(getReader(queryResponse));
        when(cristinApiClientStub.fetchGetResult(any())).thenReturn(getReader(getResponse));
        var actual = sendDefaultQuery().getBody();
        assertEquals(OBJECT_MAPPER.readTree(expected).toPrettyString(),
            OBJECT_MAPPER.readTree(actual).toPrettyString());
    }

    @Test
    void handlerReturnsNonEnrichedBodyWhenEnrichingFails() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doThrow(new IOException()).when(cristinApiClientStub).getProject(any(), any());
        handler = new FetchCristinProjects(cristinApiClientStub, environment);
        GatewayResponse<ProjectsWrapper> response = sendDefaultQuery();
        var expected = getReader(API_RESPONSE_NON_ENRICHED_PROJECTS_JSON);
        assertEquals(OBJECT_MAPPER.readTree(expected).toPrettyString(),
            OBJECT_MAPPER.readTree(response.getBody()).toPrettyString());
    }

    @Test
    public void handlerReturnsOkWhenInputContainsTitleAndLanguage() throws Exception {
        GatewayResponse<ProjectsWrapper> response = sendDefaultQuery();
        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
    }

    @Test
    public void handlerIgnoresErrorsWhenTryingToEnrichProjectInformation() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doThrow(new IOException()).when(cristinApiClientStub).getProject(any(), any());
        handler = new FetchCristinProjects(cristinApiClientStub, environment);

        GatewayResponse<ProjectsWrapper> response = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    public void handlerThrowsInternalErrorWhenQueryingProjectsFails() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doThrow(new IOException()).when(cristinApiClientStub).queryAndEnrichProjects(any(), any());
        handler = new FetchCristinProjects(cristinApiClientStub, environment);

        GatewayResponse<ProjectsWrapper> response = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    public void handlerThrowsBadRequestWhenMissingTitleQueryParameter() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(LANGUAGE_QUERY_PARAMETER, LANGUAGE_NB));

        handler.handleRequest(input, output, context);
        GatewayResponse<ProjectsWrapper> response = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    public void handlerSetsDefaultValueForMissingOptionalLanguageParameterAndReturnOk() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(TITLE_QUERY_PARAMETER, TITLE_REINDEER));

        handler.handleRequest(input, output, context);
        GatewayResponse<ProjectsWrapper> response = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    public void handlerReceivesAllowOriginHeaderValueFromEnvironmentAndPutsItOnResponse() throws Exception {
        GatewayResponse<ProjectsWrapper> response = sendDefaultQuery();
        assertEquals(ALLOW_ALL_ORIGIN, response.getHeaders().get(ApiGatewayHandler.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    public void handlerReturnsBadRequestWhenTitleQueryParamIsEmpty() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(TITLE_QUERY_PARAMETER, EMPTY_STRING));

        handler.handleRequest(input, output, context);
        GatewayResponse<ProjectsWrapper> response = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertTrue(response.getBody().contains(FetchCristinProjects.TITLE_MISSING_OR_HAS_ILLEGAL_CHARACTERS));
    }

    @Test
    public void handlerReturnsBadRequestWhenReceivingTitleQueryParamWithIllegalCharacters() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(TITLE_QUERY_PARAMETER, TITLE_ILLEGAL_CHARACTERS));

        handler.handleRequest(input, output, context);
        GatewayResponse<ProjectsWrapper> response = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertTrue(response.getBody().contains(FetchCristinProjects.TITLE_MISSING_OR_HAS_ILLEGAL_CHARACTERS));
    }

    @Test
    public void handlerReturnsBadRequestWhenReceivingInvalidLanguageQueryParam() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(TITLE_QUERY_PARAMETER, TITLE_REINDEER,
            LANGUAGE_KEY, LANGUAGE_INVALID));

        handler.handleRequest(input, output, context);
        GatewayResponse<ProjectsWrapper> response = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertTrue(response.getBody().contains(FetchCristinProjects.LANGUAGE_INVALID));
    }

    @Test
    public void cristinApiClientWillStillGenerateQueryProjectsUrlEvenWithoutParameters()
        throws IOException, URISyntaxException {
        cristinApiClientStub.generateQueryProjectsUrl(null);
    }

    @Test
    public void readerThrowsIoExceptionWhenReadingInvalidJson() {
        InputStream inputStream = new ByteArrayInputStream(INVALID_JSON.getBytes(StandardCharsets.UTF_8));
        InputStreamReader reader = new InputStreamReader(inputStream);
        Executable action = () -> CristinApiClient.fromJson(reader, CristinProject.class);
        assertThrows(IOException.class, action);
    }

    @Test
    void returnNvaProjectWhenCallingNvaProjectBuilderMethodWithValidCrisinProject() throws Exception {
        var expected = getReader(API_RESPONSE_ONE_CRISTIN_PROJECT_TO_NVA_PROJECT_JSON);
        var cristinGetProject = getReader(TestPairProvider.CRISTIN_GET_PROJECT_RESPONSE);
        CristinProject cristinProject =
            attempt(() -> JsonUtils.objectMapper.readValue(cristinGetProject, CristinProject.class)).get();
        NvaProject nvaProject = NvaProjectBuilder.getNvaProjectFromCristinProject(cristinProject);
        var actual = attempt(() -> JsonUtils.objectMapper.writeValueAsString(nvaProject)).get();

        assertEquals(OBJECT_MAPPER.readTree(expected).toPrettyString(),
            OBJECT_MAPPER.readTree(actual).toPrettyString());
    }

    private GatewayResponse<ProjectsWrapper> sendDefaultQuery() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(TITLE_QUERY_PARAMETER, TITLE_REINDEER,
            LANGUAGE_QUERY_PARAMETER, LANGUAGE_NB));
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream requestWithQueryParameters(Map<String, String> map) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(JsonUtils.objectMapper)
            .withBody(null)
            .withQueryParameters(map)
            .build();
    }

    private InputStreamReader getReader(String resource) {
        InputStream queryResultsAsStream = IoUtils.inputStreamFromResources(resource);
        return new InputStreamReader(queryResultsAsStream, Charsets.UTF_8);
    }
}
