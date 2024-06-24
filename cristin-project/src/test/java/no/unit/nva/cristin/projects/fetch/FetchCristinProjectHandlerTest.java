package no.unit.nva.cristin.projects.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import java.net.http.HttpClient;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
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

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.util.Map.of;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_IDENTIFIER;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_SERVER_ERROR;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_UNSUPPORTED_CONTENT_TYPE;
import static no.unit.nva.cristin.common.client.ApiClient.RETURNED_403_FORBIDDEN_TRY_AGAIN_LATER;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.projects.fetch.FetchCristinProjectClientStub.CRISTIN_GET_PROJECT_RESPONSE_JSON_FILE;
import static no.unit.nva.cristin.projects.fetch.FetchCristinProjectHandler.ERROR_MESSAGE_CLIENT_SENT_UNSUPPORTED_QUERY_PARAM;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.CRISTIN_ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.projects.model.cristin.adapter.CristinFundingSourceToFunding.FUNDING_SOURCES;
import static no.unit.nva.cristin.projects.model.nva.Funding.UNCONFIRMED_FUNDING;
import static no.unit.nva.testutils.RandomDataGenerator.randomInstant;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import static nva.commons.apigateway.AccessRight.MANAGE_OWN_RESOURCES;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
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
    private static final String API_RESPONSE_ONE_PROJECT_JSON =
        "nvaApiGetResponseOneNvaProject.json";
    private static final String CRISTIN_PROJECT_WITHOUT_INSTITUTION_AND_PARTICIPANTS_JSON =
        "cristinProjectWithoutInstitutionAndParticipants.json";
    private static final String NOT_AN_ID = "Not an ID";
    private static final String DEFAULT_IDENTIFIER = "9999";
    private static final String JSON_WITH_MISSING_REQUIRED_DATA = "{\"cristin_project_id\": \"456789\"}";
    private static final String DEFAULT_ACCEPT_HEADER = "*/*";
    public static final String INVALID_QUERY_PARAM_KEY = "invalid";
    public static final String INVALID_QUERY_PARAM_VALUE = "value";
    public static final String PERSON_ID = "12345";
    public static final String LANGUAGE_NB = "nb";

    private FetchCristinProjectApiClient cristinApiClientStub;
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private FetchCristinProjectHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        cristinApiClientStub = new FetchCristinProjectClientStub();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();

        var mockHttpClient = mock(HttpClient.class);
        doReturn(fakeOkResponse()).when(mockHttpClient).<String>send(any(), any());
        var fakeAuthorizedClient = new FetchCristinProjectApiClient(mockHttpClient);

        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
        handler = spy(handler);
        doReturn(fakeAuthorizedClient).when(handler).authorizedApiClient();
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
        String expected = getBodyFromResource(API_RESPONSE_ONE_PROJECT_JSON);
        assertEquals(OBJECT_MAPPER.readValue(expected, NvaProject.class),
                gatewayResponse.getBodyObject(NvaProject.class));
    }

    @Test
    void handlerReturnsBadGatewayWhenBackendThrowsException() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED)).when(cristinApiClientStub)
            .getProject(any());
        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void handlerReturnsServerErrorExceptionWhenBackendThrowsGenericException() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);

        doThrow(RuntimeException.class).when(cristinApiClientStub).getProject(any());
        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
        GatewayResponse<NvaProject> gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_SERVER_ERROR));
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

        try (var input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                 .withBody(null)
                 .withPathParameters(of(IDENTIFIER, DEFAULT_IDENTIFIER))
                 .withHeaders(of(HttpHeaders.ACCEPT, contentTypeRequested))
                 .build()) {
            handler.handleRequest(input, output, context);
        }

        var gatewayResponse = GatewayResponse.fromOutputStream(output, NvaProject.class);

        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(contentTypeRequested, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void handlerReturnsDefaultContentTypeWhenAcceptHeaderSetToDefault() throws Exception {
        try (var input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                     .withBody(null)
                     .withPathParameters(of(IDENTIFIER, DEFAULT_IDENTIFIER))
                     .withHeaders(of(HttpHeaders.ACCEPT, DEFAULT_ACCEPT_HEADER))
                     .build()) {
            handler.handleRequest(input, output, context);
        }

        var gatewayResponse = GatewayResponse.fromOutputStream(output, NvaProject.class);

        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(MediaType.JSON_UTF_8.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/jpeg", "application/xml", "application/rdf+xml", "text/plain"})
    void handlerThrowsNotAcceptableWhenAcceptHeaderUnsupported(String contentTypeRequested)
        throws Exception {

        try (var input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                     .withBody(null)
                     .withPathParameters(of(IDENTIFIER, DEFAULT_IDENTIFIER))
                     .withHeaders(of(HttpHeaders.ACCEPT, contentTypeRequested))
                     .build()) {
            handler.handleRequest(input, output, context);
        }

        var gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);

        assertEquals(HttpURLConnection.HTTP_UNSUPPORTED_TYPE, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBodyObject(Problem.class).getDetail(),
            containsString(String.format(ERROR_MESSAGE_UNSUPPORTED_CONTENT_TYPE, contentTypeRequested)));
    }

    @Test
    void handlerReturnsNvaProjectContainingFundingFromCristinWhenFundingHasValuesInCristin() throws Exception {

        var gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);
        final var expectedNvaProject = OBJECT_MAPPER.readValue(
            getBodyFromResource(API_RESPONSE_ONE_PROJECT_JSON), NvaProject.class);

        expectedNvaProject.setFunding(notRandomFunding());
        final var actualNvaProject = OBJECT_MAPPER.readValue(gatewayResponse.getBody(), NvaProject.class);

        assertEquals(expectedNvaProject, actualNvaProject);
    }

    @Test
    void handlerThrowsBadRequestWhenQueryParamsIsNotSupported() throws Exception {
        try (var input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                     .withBody(null)
                     .withQueryParameters(of(INVALID_QUERY_PARAM_KEY, INVALID_QUERY_PARAM_VALUE))
                     .withPathParameters(of(IDENTIFIER, DEFAULT_IDENTIFIER))
                     .build()) {
            handler.handleRequest(input, output, context);
        }

        var gatewayResponse = GatewayResponse.fromOutputStream(output, Problem.class);
        var body = gatewayResponse.getBodyObject(Problem.class);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(body.getDetail(), containsString(ERROR_MESSAGE_CLIENT_SENT_UNSUPPORTED_QUERY_PARAM));
    }

    @Test
    void handlerReturnsNvaProjectContainingStatusFromCristinWhenStatusIsValid() throws Exception {

        final var gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);
        final var actualNvaProject = gatewayResponse.getBodyObject(NvaProject.class);
        final var expectedProjectStatus = ProjectStatus.ACTIVE;
        assertEquals(expectedProjectStatus, actualNvaProject.getStatus());
    }

    @Test
    void handlerReturnsNvaProjectWithSummaryWhenCristinProjectHasAcademicSummary() throws Exception {
        final var summaryLanguage = randomLanguageCode();
        final var summary = randomSummary();
        final var expectedSummary = Map.of(summaryLanguage, summary);
        final var cristinApiClient =
            createCristinApiClientWithAcademicSummary(summaryLanguage, summary);
        try (InputStream input = requestWithIdentifier(of(IDENTIFIER, DEFAULT_IDENTIFIER))) {
            var outputStream = new ByteArrayOutputStream();
            new FetchCristinProjectHandler(cristinApiClient, environment)
                    .handleRequest(input, outputStream, mock(Context.class));
            final var gatewayResponse =
                GatewayResponse.fromOutputStream(outputStream, NvaProject.class);
            final var actualNvaProject = OBJECT_MAPPER.readValue(gatewayResponse.getBody(), NvaProject.class);
            assertEquals(expectedSummary, actualNvaProject.getAcademicSummary());
        }
    }

    @Test
    void shouldReturnBadGatewayWhenUpstreamReturnForbiddenIndicationMissingAllowHeader() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        var payload = getBodyFromResource(CRISTIN_PROJECT_WITHOUT_INSTITUTION_AND_PARTICIPANTS_JSON);
        doReturn(new HttpResponseFaker(payload, 403)).when(cristinApiClientStub).fetchGetResult(any(URI.class));

        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
        var gatewayResponse = sendQueryWithId(DEFAULT_IDENTIFIER);

        assertThat(gatewayResponse.getStatusCode(), equalTo(HttpURLConnection.HTTP_BAD_GATEWAY));
        assertThat(gatewayResponse.getBody(), containsString(RETURNED_403_FORBIDDEN_TRY_AGAIN_LATER));
    }

    @Test
    void shouldThrowForbiddenWhenUserRequestingRestrictedProjectWithoutHavingRequiredRights() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(new HttpResponseFaker(EMPTY_STRING, HTTP_UNAUTHORIZED)).when(cristinApiClientStub)
            .fetchGetResult(any());

        var mockHttpClient = mock(HttpClient.class);
        doReturn(fakeOkResponse()).when(mockHttpClient).<String>send(any(), any());
        var fakeAuthorizedClient = new FetchCristinProjectApiClient(mockHttpClient);

        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
        handler = spy(handler);
        doReturn(fakeAuthorizedClient).when(handler).authorizedApiClient();

        var gatewayResponse =
            sendQueryWithPersonIdAndAccessRight(MANAGE_OWN_RESOURCES);
        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_FORBIDDEN));
    }

    @Test
    void shouldReturnRestrictedProjectWhenUserHasRequiredRights() throws Exception {
        cristinApiClientStub = spy(cristinApiClientStub);
        doReturn(new HttpResponseFaker(EMPTY_STRING, HTTP_UNAUTHORIZED)).when(cristinApiClientStub)
            .fetchGetResult(any());

        var mockHttpClient = mock(HttpClient.class);
        doReturn(fakeOkResponseWithCreatorSameAsUser()).when(mockHttpClient).<String>send(any(), any());
        var fakeAuthorizedClient = new FetchCristinProjectApiClient(mockHttpClient);

        handler = new FetchCristinProjectHandler(cristinApiClientStub, environment);
        handler = spy(handler);
        doReturn(fakeAuthorizedClient).when(handler).authorizedApiClient();

        var gatewayResponse =
            sendQueryWithPersonIdAndAccessRight(MANAGE_OWN_RESOURCES);
        assertThat(gatewayResponse.getStatusCode(), equalTo(HTTP_OK));
    }

    private HttpResponseFaker fakeOkResponse() {
        return new HttpResponseFaker(basicCristinProjectWithOnlyRequiredFields().toString(), HTTP_OK);
    }

    private HttpResponseFaker fakeOkResponseWithCreatorSameAsUser() {
        return new HttpResponseFaker(cristinProjectWithCreatorData().toString(), HTTP_OK);
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
        ((ObjectNode) cristinProjectSource).set(CRISTIN_ACADEMIC_SUMMARY, summaryNode);
        return new FetchCristinProjectClientStub(OBJECT_MAPPER.writeValueAsString(cristinProjectSource));
    }

    private List<Funding> notRandomFunding() {
        final var source = UriWrapper.fromUri(getNvaApiUri(FUNDING_SOURCES)).addChild("NFR").getUri();
        final var identifier = "654321";
        final var labels = Map.of("en", "Research Council of Norway (RCN)");
        return List.of(new Funding(UNCONFIRMED_FUNDING, source, identifier, labels));
    }

    private GatewayResponse<NvaProject> sendQueryWithId(String identifier) throws IOException {
        try (var input = requestWithIdentifier(of(IDENTIFIER, identifier))) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, NvaProject.class);
    }

    private InputStream requestWithIdentifier(Map<String, String> idPathParam)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withPathParameters(idPathParam)
            .build();
    }

    private String getBodyFromResource(String resource) {
        return IoUtils.stringFromResources(Path.of(resource));
    }

    private GatewayResponse<NvaProject> sendQueryWithPersonIdAndAccessRight(AccessRight accessRight)
        throws IOException {
        var input = requestWithPersonIdAndAccessRight(accessRight);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, NvaProject.class);
    }

    private InputStream requestWithPersonIdAndAccessRight(AccessRight accessRight)
        throws JsonProcessingException {

        var customerId = randomUri();
        var personIdUri = UriWrapper.fromUri(randomUri()).addChild(PERSON_ID).getUri();

        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                   .withBody(null)
                   .withPersonCristinId(personIdUri)
                   .withCurrentCustomer(customerId)
                   .withAccessRights(customerId, accessRight)
                   .withPathParameters(of(IDENTIFIER, DEFAULT_IDENTIFIER))
                   .build();
    }

    private CristinProject basicCristinProjectWithOnlyRequiredFields() {
        var cristinProject = new CristinProject();
        cristinProject.setCristinProjectId(randomInteger(999999).toString());
        cristinProject.setTitle(Map.of(LANGUAGE_NB, randomString()));
        cristinProject.setStartDate(randomInstant());
        cristinProject.setCoordinatingInstitution(CristinOrganization.fromIdentifier("185"));
        return cristinProject;
    }

    private CristinProject cristinProjectWithCreatorData() {
        var cristinProject = basicCristinProjectWithOnlyRequiredFields();
        cristinProject.setCreator(cristinPersonWithDefaultIdentifier());
        return cristinProject;
    }

    private CristinPerson cristinPersonWithDefaultIdentifier() {
        return new CristinPerson.Builder()
                   .withCristinPersonId(PERSON_ID)
                   .build();
    }

}
