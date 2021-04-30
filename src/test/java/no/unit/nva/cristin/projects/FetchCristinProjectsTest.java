package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.Constants.CRISTIN_LANGUAGE_PARAM;
import static no.unit.nva.cristin.projects.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static no.unit.nva.cristin.projects.Constants.TITLE;
import static no.unit.nva.cristin.projects.CristinHandler.LANGUAGE_INVALID_ERROR_MESSAGE;
import static no.unit.nva.cristin.projects.CristinHandler.LANGUAGE_QUERY_PARAMETER;
import static nva.commons.apigateway.ApiGatewayHandler.APPLICATION_PROBLEM_JSON;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

public class FetchCristinProjectsTest {

    private static final String LANGUAGE_KEY = "language";
    private static final String EMPTY_STRING = "";
    private static final String LANGUAGE_NB = "nb";
    private static final String INVALID_LANGUAGE_PARAM = "ru";
    private static final String RANDOM_TITLE = "reindeer";
    private static final String TITLE_ILLEGAL_CHARACTERS = "abc123- ,-?";
    private static final String INVALID_JSON = "This is not valid JSON!";
    private static final String EMPTY_LIST_STRING = "[]";

    private static final String ALLOW_ALL_ORIGIN = "*";
    private static final String API_RESPONSE_NON_ENRICHED_PROJECTS_JSON = "api_response_non_enriched_projects.json";
    private static final String API_RESPONSE_ONE_CRISTIN_PROJECT_TO_NVA_PROJECT_JSON =
        "api_response_one_cristin_project_to_nva_project.json";
    private static final String CRISTIN_GET_PROJECT_RESPONSE = "cristinGetProjectResponse.json";
    private static final String API_QUERY_RESPONSE_NO_PROJECTS_FOUND_JSON = "api_query_response_no_projects_found.json";
    private static final String GET_CRISTIN_PROJECTS_EXAMPLE_URI = "https://api.cristin"
        + ".no/v2/projects/?lang=nb&title=reindeer";
    private CristinApiClient cristinApiClientStub;
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private FetchCristinProjects handler;

    @BeforeEach
    void setUp() {
        cristinApiClientStub = new CristinApiClientStub();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new FetchCristinProjects(cristinApiClientStub, environment);
    }

    @ParameterizedTest
    @ArgumentsSource(TestPairProvider.class)
    void handlerReturnsExpectedBodyWhenRequestInputIsValid(String expected) throws IOException {
        var actual = sendDefaultQuery().getBody();
        assertEquals(OBJECT_MAPPER.readTree(expected), OBJECT_MAPPER.readTree(actual));
    }

    @Test
    public void handlerIgnoresErrorsWhenTryingToEnrichProjectInformation() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doThrow(new BadGatewayException(null)).when(cristinApiClientStub).getProject(any(), any());
        handler = new FetchCristinProjects(cristinApiClientStub, environment);

        GatewayResponse<ProjectsWrapper> response = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    public void handlerReturnsOkWhenInputContainsTitleAndLanguage() throws Exception {
        GatewayResponse<ProjectsWrapper> response = sendDefaultQuery();
        assertEquals(HttpURLConnection.HTTP_OK, response.getStatusCode());
    }

    @Test
    public void handlerThrowsInternalErrorWhenQueryingProjectsFails() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doThrow(new RuntimeException()).when(cristinApiClientStub).queryAndEnrichProjects(any());
        handler = new FetchCristinProjects(cristinApiClientStub, environment);

        GatewayResponse<ProjectsWrapper> response = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerReturnsNonEnrichedBodyWhenEnrichingFails() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doThrow(new BadGatewayException(null)).when(cristinApiClientStub).getProject(any(), any());
        handler = new FetchCristinProjects(cristinApiClientStub, environment);
        GatewayResponse<ProjectsWrapper> response = sendDefaultQuery();
        var expected = IoUtils.stringFromResources(Path.of(API_RESPONSE_NON_ENRICHED_PROJECTS_JSON));
        assertEquals(OBJECT_MAPPER.readTree(expected), OBJECT_MAPPER.readTree(response.getBody()));
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
        InputStream input = requestWithQueryParameters(Map.of(TITLE, RANDOM_TITLE));

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
        InputStream input = requestWithQueryParameters(Map.of(TITLE, EMPTY_STRING));

        handler.handleRequest(input, output, context);
        GatewayResponse<ProjectsWrapper> response = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertTrue(response.getBody().contains(FetchCristinProjects.TITLE_MISSING_OR_HAS_ILLEGAL_CHARACTERS));
    }

    @Test
    public void handlerReturnsBadRequestWhenReceivingTitleQueryParamWithIllegalCharacters() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(TITLE, TITLE_ILLEGAL_CHARACTERS));

        handler.handleRequest(input, output, context);
        GatewayResponse<ProjectsWrapper> response = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertTrue(response.getBody().contains(FetchCristinProjects.TITLE_MISSING_OR_HAS_ILLEGAL_CHARACTERS));
    }

    @Test
    public void handlerReturnsBadRequestWhenReceivingInvalidLanguageQueryParam() throws Exception {
        InputStream input = requestWithQueryParameters(Map.of(TITLE, RANDOM_TITLE,
            LANGUAGE_KEY, INVALID_LANGUAGE_PARAM));

        handler.handleRequest(input, output, context);
        GatewayResponse<ProjectsWrapper> response = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertTrue(response.getBody().contains(LANGUAGE_INVALID_ERROR_MESSAGE));
    }

    @Test
    public void cristinApiClientWillStillGenerateQueryProjectsUrlEvenWithoutParameters() {
        cristinApiClientStub.generateQueryProjectsUrl(null); // TODO: Remove this?
    }

    @Test
    public void readerThrowsIoExceptionWhenReadingInvalidJson() {
        InputStream inputStream = new ByteArrayInputStream(INVALID_JSON.getBytes(StandardCharsets.UTF_8));
        Executable action = () -> CristinApiClient.fromJson(inputStream, CristinProject.class);
        assertThrows(IOException.class, action);
    }

    @Test
    void returnNvaProjectWhenCallingNvaProjectBuilderMethodWithValidCristinProject() throws Exception {
        var expected = IoUtils.stringFromResources(Path.of(API_RESPONSE_ONE_CRISTIN_PROJECT_TO_NVA_PROJECT_JSON));
        var cristinGetProject = IoUtils.stringFromResources(Path.of(CRISTIN_GET_PROJECT_RESPONSE));
        CristinProject cristinProject =
            attempt(() -> OBJECT_MAPPER.readValue(cristinGetProject, CristinProject.class)).get();
        NvaProject nvaProject = new NvaProjectBuilder(cristinProject).build();
        nvaProject.setContext(PROJECT_LOOKUP_CONTEXT_URL);
        var actual = attempt(() -> OBJECT_MAPPER.writeValueAsString(nvaProject)).get();

        assertEquals(OBJECT_MAPPER.readTree(expected), OBJECT_MAPPER.readTree(actual));
    }

    @Test
    void handlerReturnsProjectsWrapperWithAllMetadataButEmptyHitsArrayWhenNoMatchesAreFoundInCristin()
        throws Exception {

        cristinApiClientStub = spy(cristinApiClientStub);
        var emptyResponse = new HttpResponseStub(IoUtils.stringToStream(EMPTY_LIST_STRING));
        doReturn(emptyResponse)
            .when(cristinApiClientStub).fetchQueryResults(any(URI.class));
        var expected = IoUtils.stringFromResources(Path.of(API_QUERY_RESPONSE_NO_PROJECTS_FOUND_JSON));

        handler = new FetchCristinProjects(cristinApiClientStub, environment);
        GatewayResponse<ProjectsWrapper> response = sendDefaultQuery();

        assertEquals(OBJECT_MAPPER.readTree(expected), OBJECT_MAPPER.readTree(response.getBody()));
    }

    @Test
    void getsCorrectUriWhenCallingQueryProjectsUriBuilder() throws Exception {
        assertEquals(new URI(GET_CRISTIN_PROJECTS_EXAMPLE_URI), cristinApiClientStub
            .generateQueryProjectsUrl(Map.of(TITLE, RANDOM_TITLE, CRISTIN_LANGUAGE_PARAM, LANGUAGE_NB)));
    }

    @Test
    void handlerReturnsServerErrorExceptionWhenBackendThrowsGenericException() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(RuntimeException.class).when(cristinApiClientStub).generateQueryProjectsUrl(any());
        handler = new FetchCristinProjects(cristinApiClientStub, environment);
        GatewayResponse<ProjectsWrapper> response = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerThrowsBadGatewayExceptionWhenThereIsThrownIoExceptionWhenReadingFromJson() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseStub(IoUtils.stringToStream("")))
            .when(cristinApiClientStub).fetchQueryResults(any(URI.class));
        handler = new FetchCristinProjects(cristinApiClientStub, environment);
        GatewayResponse<ProjectsWrapper> response = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, response.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON, response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    private GatewayResponse<ProjectsWrapper> sendDefaultQuery() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(TITLE, RANDOM_TITLE,
            LANGUAGE_QUERY_PARAMETER, LANGUAGE_NB));
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream requestWithQueryParameters(Map<String, String> map) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withQueryParameters(map)
            .build();
    }

}
