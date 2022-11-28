package no.unit.nva.cristin.projects.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.FundingSource;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
import no.unit.nva.cristin.testing.HttpResponseFaker;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.Map.of;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_IDENTIFIER;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_SERVER_ERROR;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_UNSUPPORTED_CONTENT_TYPE;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.common.handler.CristinHandler.DEFAULT_LANGUAGE_CODE;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.projects.fetch.FetchCristinProjectClientStub.CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE;
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

public class FetchCristinProjectHandlerTest {

    private static final String CRISTIN_GET_PROJECT_ID_NOT_FOUND_RESPONSE_JSON =
        "cristinGetProjectIdNotFoundResponse.json";
    private static final String API_RESPONSE_ONE_PROJECT_WITH_FUNDING_JSON =
            "api_response_one_cristin_project_to_nva_project_with_funding.json";
    private static final String CRISTIN_PROJECT_WITHOUT_INSTITUTION_AND_PARTICIPANTS_JSON =
        "cristinProjectWithoutInstitutionAndParticipants.json";
    private static final String API_RESPONSE_GET_PROJECT_WITH_MISSING_FIELDS_JSON =
        "api_response_get_project_with_missing_fields.json";
    private static final String NOT_AN_ID = "Not an ID";
    private static final String DEFAULT_IDENTIFIER = "9999";
    private static final String JSON_WITH_MISSING_REQUIRED_DATA = "{\"cristin_project_id\": \"456789\"}";
    private static final String ENGLISH_LANGUAGE = "en";
    private static final String GET_ONE_CRISTIN_PROJECT_EXAMPLE_URI =
            "https://api.cristin-test.uio.no/v2/projects/9999?lang=en";
    private static final String DEFAULT_ACCEPT_HEADER = "*/*";
    public static final String FIELD_STATUS = "status";
    public static final String NOT_LEGAL_STATUS = "not_legal_status";
    public static final String INVALID_QUERY_PARAM_KEY = "invalid";
    public static final String INVALID_QUERY_PARAM_VALUE = "value";

    private FetchCristinProjectApiClient cristinApiClientStub;
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private FetchCristinProjectHandler handler;

    @BeforeEach
    void setUp() {
        cristinApiClientStub = new FetchCristinProjectClientStub();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
    }

    @Test
    void handlerReturnsNotFoundStatusWhenIdIsNotFound() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(new HttpResponseFaker(getBodyFromResource(CRISTIN_GET_PROJECT_ID_NOT_FOUND_RESPONSE_JSON), 404))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));

        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, gatewayResponse.getStatusCode());
    }

    @Test
    void handlerReturnsBadGatewayWhenStatusCodeFromBackendSignalsError() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(new HttpResponseFaker(null, 500))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));

        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void handlerReturnsBadRequestWhenIdIsNotANumber() throws Exception {
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(NOT_AN_ID);
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_IDENTIFIER));
    }

    @Test
    void handlerReturnsNvaProjectFromTransformedCristinProjectWhenIdIsFound() throws Exception {
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);
        String expected = getBodyFromResource(API_RESPONSE_ONE_PROJECT_WITH_FUNDING_JSON);
        assertEquals(OBJECT_MAPPER.readValue(expected, NvaProject.class),
                gatewayResponse.getBodyObject(NvaProject.class));
    }

    @Test
    void handlerReturnsBadGatewayWhenBackendThrowsException() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED)).when(cristinApiClientStub)
            .getProject(any(), any());
        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void handlerReturnsServerErrorExceptionWhenBackendThrowsGenericException() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(RuntimeException.class).when(cristinApiClientStub).getProject(any(), any());
        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_SERVER_ERROR));
    }

    @Test
    void handlerReturnsNvaProjectWithoutParticipantsAndCoordinatingInstitutionIfTheyAreMissingFromBackend()
        throws Exception {

        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseFaker(getBodyFromResource(CRISTIN_PROJECT_WITHOUT_INSTITUTION_AND_PARTICIPANTS_JSON)))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));

        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);

        String expected = getBodyFromResource(API_RESPONSE_GET_PROJECT_WITH_MISSING_FIELDS_JSON);
        assertEquals(OBJECT_MAPPER.readValue(expected, NvaProject.class),
                gatewayResponse.getBodyObject(NvaProject.class));
    }

    @Test
    void handlerThrowsBadGatewayExceptionWhenBackendReturnsInvalidProjectData() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(new HttpResponseFaker(JSON_WITH_MISSING_REQUIRED_DATA))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));

        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(
            String.format(ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, DEFAULT_IDENTIFIER)));
    }

    @Test
    void getsCorrectUriWhenCallingGetProjectUriBuilder() throws Exception {
        assertEquals(new URI(GET_ONE_CRISTIN_PROJECT_EXAMPLE_URI),
            cristinApiClientStub.generateGetProjectUri(DEFAULT_IDENTIFIER, ENGLISH_LANGUAGE));
    }

    @Test
    void handlerThrowsBadGatewayExceptionWhenThereIsThrownExceptionWhenReadingFromJson() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseFaker(EMPTY_STRING))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));
        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void handlerThrowsInternalErrorWhenHttpStatusCodeIsSomeUnexpectedValue() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doReturn(new HttpResponseFaker(EMPTY_STRING, 418))
            .when(cristinApiClientStub).fetchGetResult(any(URI.class));
        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_SERVER_ERROR));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/json; charset=utf-8", "application/ld+json"})
    void handlerReturnsMatchingContentTypeBasedOnAcceptHeader(String contentTypeRequested) throws Exception {

        InputStream input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withPathParameters(of(IDENTIFIER, DEFAULT_IDENTIFIER))
            .withHeaders(of(HttpHeaders.ACCEPT, contentTypeRequested))
            .build();
        handler.handleRequest(input, output, context);

        GatewayResponse<NvaProject> gatewayResponse = GatewayResponse.fromOutputStream(output, NvaProject.class);

        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(contentTypeRequested, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerReturnsDefaultContentTypeWhenAcceptHeaderSetToDefault() throws Exception {
        InputStream input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withPathParameters(of(IDENTIFIER, DEFAULT_IDENTIFIER))
            .withHeaders(of(HttpHeaders.ACCEPT, DEFAULT_ACCEPT_HEADER))
            .build();
        handler.handleRequest(input, output, context);

        GatewayResponse<NvaProject> gatewayResponse = GatewayResponse.fromOutputStream(output, NvaProject.class);

        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(MediaType.JSON_UTF_8.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/jpeg", "application/xml", "application/rdf+xml", "text/plain"})
    void handlerThrowsNotAcceptableWhenAcceptHeaderUnsupported(String contentTypeRequested)
        throws Exception {

        InputStream input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withPathParameters(of(IDENTIFIER, DEFAULT_IDENTIFIER))
            .withHeaders(of(HttpHeaders.ACCEPT, contentTypeRequested))
            .build();
        handler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);

        assertEquals(HttpURLConnection.HTTP_UNSUPPORTED_TYPE, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBodyObject(Problem.class).getDetail(),
            containsString(String.format(ERROR_MESSAGE_UNSUPPORTED_CONTENT_TYPE, contentTypeRequested)));
    }

    @Test
    void handlerReturnsNvaProjectContainingFundingFromCristinWhenFundingHasValuesInCristin() throws Exception {

        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);
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
                .withPathParameters(of(IDENTIFIER, DEFAULT_IDENTIFIER))
                .build();
        handler.handleRequest(input, output, context);

        GatewayResponse<Problem> gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);
        Problem body = gatewayResponse.getBodyObject(Problem.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(body.getDetail(), containsString(
                validQueryParameterNamesMessage(FetchCristinProjectHandler.VALID_QUERY_PARAMETERS)));
    }

    @Test
    void handlerReturnsNvaProjectContainingStatusFromCristinWhenStatusIsValid() throws Exception {

        final GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);
        final NvaProject actualNvaProject = gatewayResponse.getBodyObject(NvaProject.class);
        final ProjectStatus expectedProjectStatus = ProjectStatus.ACTIVE;
        assertEquals(expectedProjectStatus, actualNvaProject.getStatus());
    }

    @Test
    void handlerReturnsBadGatewayWhenCristinProjectHasInvalidStatusValue() throws Exception {
        final FetchCristinProjectHandler fetchHandler =
                new FetchCristinProjectHandler(createCristinApiClientWithResponseContainingError(), environment);
        final InputStream input = requestWithLanguageAndId(
                of(LANGUAGE, DEFAULT_LANGUAGE_CODE), of(IDENTIFIER, DEFAULT_IDENTIFIER));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        fetchHandler.handleRequest(input, outputStream, mock(Context.class));
        final GatewayResponse<NvaProject> gatewayResponse =
                GatewayResponse.fromOutputStream(outputStream, NvaProject.class);
        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
    }

    @Test
    void handlerReturnsNvaProjectWithSummaryWhenCristinProjectHasAcademicSummary() throws Exception {
        final String summaryLanguage = randomLanguageCode();
        final String summary = randomSummary();
        final Map<String, String> expectedSummary = Map.of(summaryLanguage, summary);
        final FetchCristinProjectApiClient cristinApiClient =
            createCristinApiClientWithAcademicSummary(summaryLanguage, summary);
        final InputStream input = requestWithLanguageAndId(
                of(LANGUAGE, DEFAULT_LANGUAGE_CODE), of(IDENTIFIER, DEFAULT_IDENTIFIER));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new FetchCristinProjectHandler(cristinApiClient, environment)
                .handleRequest(input, outputStream, mock(Context.class));
        final GatewayResponse<NvaProject> gatewayResponse =
                GatewayResponse.fromOutputStream(outputStream, NvaProject.class);
        final NvaProject actualNvaProject = OBJECT_MAPPER.readValue(gatewayResponse.getBody(), NvaProject.class);
        assertEquals(expectedSummary, actualNvaProject.getAcademicSummary());
    }

    private String randomLanguageCode() {
        return "lalaland";
    }

    private String randomSummary() {
        return "summary summary summary";
    }

    private FetchCristinProjectClientStub createCristinApiClientWithAcademicSummary(String language, String summary)
            throws JsonProcessingException {
        JsonNode cristinProjectSource =
                OBJECT_MAPPER.readTree(IoUtils.stringFromResources(Path.of(CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE)));
        ObjectNode summaryNode = JsonNodeFactory.instance.objectNode().put(language, summary);
        ((ObjectNode) cristinProjectSource).set(ACADEMIC_SUMMARY, summaryNode);
        return new FetchCristinProjectClientStub(OBJECT_MAPPER.writeValueAsString(cristinProjectSource));
    }


    private FetchCristinProjectClientStub createCristinApiClientWithResponseContainingError()
        throws JsonProcessingException {
        JsonNode cristinProjectSource =
                OBJECT_MAPPER.readTree(IoUtils.stringFromResources(Path.of(CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE)));
        ((ObjectNode) cristinProjectSource).put(FIELD_STATUS, NOT_LEGAL_STATUS);
        return new FetchCristinProjectClientStub(OBJECT_MAPPER.writeValueAsString(cristinProjectSource));
    }

    private List<Funding> notRandomFunding() {
        final String fundingSourceCode = "NFR";
        final String language = "en";
        final String name = "Research Council of Norway (RCN)";
        final String fundingCode = "654321";
        FundingSource fundingSource = new FundingSource(Map.of(language, name), fundingSourceCode);
        return List.of(new Funding(fundingSource,fundingCode));
    }

    private GatewayResponse<NvaProject> sendQueryWithId(String identifier) throws IOException {
        InputStream input = requestWithLanguageAndId(
            of(LANGUAGE, DEFAULT_LANGUAGE_CODE),
            of(IDENTIFIER, identifier));
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, NvaProject.class);
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