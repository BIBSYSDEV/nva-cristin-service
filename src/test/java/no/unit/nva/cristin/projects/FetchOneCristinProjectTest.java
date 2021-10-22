package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.MediaType;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.FundingSource;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.Problem;

import javax.ws.rs.core.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.Map.of;
import static no.unit.nva.cristin.projects.Constants.ID;
import static no.unit.nva.cristin.projects.Constants.LANGUAGE;
import static no.unit.nva.cristin.projects.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.CristinApiClientStub.CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE;
import static no.unit.nva.cristin.projects.CristinHandler.DEFAULT_LANGUAGE_CODE;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_SERVER_ERROR;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_UNSUPPORTED_CONTENT_TYPE;
import static no.unit.nva.cristin.projects.FetchCristinProjectsTest.INVALID_QUERY_PARAM_KEY;
import static no.unit.nva.cristin.projects.FetchCristinProjectsTest.INVALID_QUERY_PARAM_VALUE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.ACADEMIC_SUMMARY;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class FetchOneCristinProjectTest {

    private static final String CRISTIN_GET_PROJECT_ID_NOT_FOUND_RESPONSE_JSON =
        "cristinGetProjectIdNotFoundResponse.json";
    private static final String API_RESPONSE_ONE_PROJECT_WITH_FUNDING_JSON =
            "api_response_one_cristin_project_to_nva_project_with_funding.json";
    private static final String CRISTIN_PROJECT_WITHOUT_INSTITUTION_AND_PARTICIPANTS_JSON =
        "cristinProjectWithoutInstitutionAndParticipants.json";
    private static final String API_RESPONSE_GET_PROJECT_WITH_MISSING_FIELDS_JSON =
        "api_response_get_project_with_missing_fields.json";
    private static final String NOT_AN_ID = "Not an ID";
    private static final String DEFAULT_ID = "9999";
    private static final String JSON_WITH_MISSING_REQUIRED_DATA = "{\"cristin_project_id\": \"456789\"}";
    private static final String ENGLISH_LANGUAGE = "en";
    private static final String GET_ONE_CRISTIN_PROJECT_EXAMPLE_URI = "https://api.cristin.no/v2/projects/9999?lang=en";
    private static final String DEFAULT_ACCEPT_HEADER = "*/*";
    public static final String FIELD_STATUS = "status";
    public static final String NOT_LEGAL_STATUS = "not_legal_status";

    private CristinApiClient cristinApiClientStub;
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private FetchOneCristinProject handler;

    @BeforeEach
    void setUp() {
        cristinApiClientStub = new CristinApiClientStub();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
    }

    @Test
    void handlerReturnsNotFoundStatusWhenIdIsNotFound() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(new HttpResponseStub(getBodyFromResource(CRISTIN_GET_PROJECT_ID_NOT_FOUND_RESPONSE_JSON), 404))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));

        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, gatewayResponse.getStatusCode());
    }

    @Test
    void handlerReturnsBadGatewayWhenStatusCodeFromBackendSignalsError() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(new HttpResponseStub(null, 500))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));

        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void handlerReturnsBadRequestWhenIdIsNotANumber() throws Exception {
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(NOT_AN_ID);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID));
    }

    @Test
    void handlerReturnsNvaProjectFromTransformedCristinProjectWhenIdIsFound() throws Exception {
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);
        String expected = getBodyFromResource(API_RESPONSE_ONE_PROJECT_WITH_FUNDING_JSON);
        assertEquals(OBJECT_MAPPER.readValue(expected, NvaProject.class),
                gatewayResponse.getBodyObject(NvaProject.class));
    }

    @Test
    void handlerReturnsBadGatewayWhenBackendThrowsException() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED)).when(cristinApiClientStub)
            .getProject(any(), any());
        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void handlerReturnsServerErrorExceptionWhenBackendThrowsGenericException() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(RuntimeException.class).when(cristinApiClientStub).getProject(any(), any());
        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_SERVER_ERROR));
    }

    @Test
    void handlerReturnsNvaProjectWithoutParticipantsAndCoordinatingInstitutionIfTheyAreMissingFromBackend()
        throws Exception {

        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseStub(getBodyFromResource(CRISTIN_PROJECT_WITHOUT_INSTITUTION_AND_PARTICIPANTS_JSON)))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));

        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        String expected = getBodyFromResource(API_RESPONSE_GET_PROJECT_WITH_MISSING_FIELDS_JSON);
        assertEquals(OBJECT_MAPPER.readValue(expected, NvaProject.class),
                gatewayResponse.getBodyObject(NvaProject.class));
    }

    @Test
    void handlerThrowsBadGatewayExceptionWhenBackendReturnsInvalidProjectData() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(new HttpResponseStub(JSON_WITH_MISSING_REQUIRED_DATA))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));

        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(
            String.format(ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, DEFAULT_ID)));
    }

    @Test
    void getsCorrectUriWhenCallingGetProjectUriBuilder() throws Exception {
        assertEquals(new URI(GET_ONE_CRISTIN_PROJECT_EXAMPLE_URI),
            cristinApiClientStub.generateGetProjectUri(DEFAULT_ID, ENGLISH_LANGUAGE));
    }

    @Test
    void handlerThrowsBadGatewayExceptionWhenThereIsThrownExceptionWhenReadingFromJson() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseStub(EMPTY_STRING))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));
        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void handlerReturnsInternalErrorWhenUriCreationFails() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(URISyntaxException.class).when(cristinApiClientStub).generateGetProjectUri(any(), any());

        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_SERVER_ERROR));
    }

    @Test
    void handlerThrowsInternalErrorWhenHttpStatusCodeIsSomeUnexpectedValue() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseStub(EMPTY_STRING, 418))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));
        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_SERVER_ERROR));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/json; charset=utf-8", "application/ld+json"})
    void handlerReturnsMatchingContentTypeBasedOnAcceptHeader(String contentTypeRequested) throws Exception {

        InputStream input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withPathParameters(of(ID, DEFAULT_ID))
            .withHeaders(of(HttpHeaders.ACCEPT, contentTypeRequested))
            .build();
        handler.handleRequest(input, output, context);

        GatewayResponse<NvaProject> gatewayResponse = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(contentTypeRequested, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerReturnsDefaultContentTypeWhenAcceptHeaderSetToDefault() throws Exception {
        InputStream input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withPathParameters(of(ID, DEFAULT_ID))
            .withHeaders(of(HttpHeaders.ACCEPT, DEFAULT_ACCEPT_HEADER))
            .build();
        handler.handleRequest(input, output, context);

        GatewayResponse<NvaProject> gatewayResponse = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(MediaType.JSON_UTF_8.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/jpeg", "application/xml", "application/rdf+xml", "text/plain"})
    void handlerThrowsNotAcceptableWhenAcceptHeaderUnsupported(String contentTypeRequested)
        throws Exception {

        InputStream input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withPathParameters(of(ID, DEFAULT_ID))
            .withHeaders(of(HttpHeaders.ACCEPT, contentTypeRequested))
            .build();
        handler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_UNSUPPORTED_TYPE, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBodyObject(Problem.class).getDetail(),
            containsString(String.format(ERROR_MESSAGE_UNSUPPORTED_CONTENT_TYPE, contentTypeRequested)));
    }

    @Test
    void handlerReturnsNvaProjectContainingFundingFromCristinWhenFundingHasValuesInCristin() throws Exception {

        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);
        final NvaProject expectedNvaProject = OBJECT_MAPPER.readValue(
                getBodyFromResource(API_RESPONSE_ONE_PROJECT_WITH_FUNDING_JSON), NvaProject.class);
        final List<Funding> funding = notRandomFunding();
        expectedNvaProject.setFunding(funding);
        final NvaProject actualNvaProject = OBJECT_MAPPER.readValue(gatewayResponse.getBody(), NvaProject.class);

        assertEquals(expectedNvaProject, actualNvaProject);
    }

    @Test
    void handlerThrowsBadRequestWhenQueryParamsIsNotSupported() throws Exception {
        InputStream input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                .withBody(null)
                .withQueryParameters(of(INVALID_QUERY_PARAM_KEY, INVALID_QUERY_PARAM_VALUE))
                .withPathParameters(of(ID, DEFAULT_ID))
                .build();
        handler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output);
        Problem body = gatewayResponse.getBodyObject(Problem.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(body.getDetail(), containsString(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP));
    }

    @Test
    void handlerReturnsNvaProjectContainingStatusFromCristinWhenStatusHasLegalValue() throws Exception {

        final GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_ID);
        final NvaProject actualNvaProject = OBJECT_MAPPER.readValue(gatewayResponse.getBody(), NvaProject.class);
        final ProjectStatus expectedProjectStatus = ProjectStatus.ACTIVE;
        assertEquals(expectedProjectStatus, actualNvaProject.getStatus());
    }

    @Test
    void handlerReturnsHttp502WhenStatusHasIllegalValue() throws Exception {
        final FetchOneCristinProject fetchHandler =
                new FetchOneCristinProject(createCristinApiClientWithResponseContainingError(), environment);
        final InputStream input = requestWithLanguageAndId(of(LANGUAGE, DEFAULT_LANGUAGE_CODE), of(ID, DEFAULT_ID));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        fetchHandler.handleRequest(input, outputStream, mock(Context.class));
        final GatewayResponse<NvaProject> gatewayResponse = GatewayResponse.fromOutputStream(outputStream);
        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
    }

    @Test
    void handlerReturnsNvaProjectWithSummaryWhenCristinProjectHasAcademicSummary() throws Exception {
        final String summaryLanguage = randomLanguageCode();
        final String summary = randomSummary();
        final Map<String, String> expectedSummary = Map.of(summaryLanguage, summary);
        final CristinApiClient cristinApiClient = createCristinApiClientWithAcademicSummary(summaryLanguage, summary);
        final InputStream input = requestWithLanguageAndId(of(LANGUAGE, DEFAULT_LANGUAGE_CODE), of(ID, DEFAULT_ID));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new FetchOneCristinProject(cristinApiClient, environment)
                .handleRequest(input, outputStream, mock(Context.class));
        final GatewayResponse<NvaProject> gatewayResponse = GatewayResponse.fromOutputStream(outputStream);
        final NvaProject actualNvaProject = OBJECT_MAPPER.readValue(gatewayResponse.getBody(), NvaProject.class);
        assertEquals(expectedSummary, actualNvaProject.getAcademicSummary());
    }

    private String randomLanguageCode() {
        return "lalaland";
    }

    private String randomSummary() {
        return "summary summary summary";
    }

    private CristinApiClient createCristinApiClientWithAcademicSummary(String language, String summary)
            throws JsonProcessingException {
        JsonNode cristinProjectSource =
                OBJECT_MAPPER.readTree(IoUtils.stringFromResources(Path.of(CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE)));
        ObjectNode summaryNode = JsonNodeFactory.instance.objectNode().put(language, summary);
        ((ObjectNode) cristinProjectSource).set(ACADEMIC_SUMMARY, summaryNode);
        return new CristinApiClientStub(OBJECT_MAPPER.writeValueAsString(cristinProjectSource));
    }


    private CristinApiClient createCristinApiClientWithResponseContainingError() throws JsonProcessingException {
        JsonNode cristinProjectSource =
                OBJECT_MAPPER.readTree(IoUtils.stringFromResources(Path.of(CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE)));
        ((ObjectNode) cristinProjectSource).put(FIELD_STATUS, NOT_LEGAL_STATUS);
        return new CristinApiClientStub(OBJECT_MAPPER.writeValueAsString(cristinProjectSource));
    }

    private List<Funding> notRandomFunding() {
        final String fundingSourceCode = "NFR";
        final String language = "en";
        final String name = "Research Council of Norway (RCN)";
        final String fundingCode = "654321";
        FundingSource fundingSource = new FundingSource(Map.of(language, name), fundingSourceCode);
        return List.of(new Funding(fundingSource,fundingCode));
    }

    private GatewayResponse<NvaProject> sendQueryWithId(String id) throws IOException {
        InputStream input = requestWithLanguageAndId(
            of(LANGUAGE, DEFAULT_LANGUAGE_CODE),
            of(ID, id));
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream requestWithLanguageAndId(Map<String, String> languageQueryParam,
                                                 Map<String, String> idPathParam)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withQueryParameters(languageQueryParam)
            .withPathParameters(idPathParam)
            .build();
    }

    private String getBodyFromResource(String resource) {
        return IoUtils.stringFromResources(Path.of(resource));
    }
}
