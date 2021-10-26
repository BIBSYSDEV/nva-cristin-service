package no.unit.nva.cristin.projects;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.common.util.UriUtils.queryParameters;
import static no.unit.nva.cristin.projects.Constants.LANGUAGE;
import static no.unit.nva.cristin.projects.Constants.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.projects.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.Constants.PAGE;
import static no.unit.nva.cristin.projects.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static no.unit.nva.cristin.projects.Constants.QUERY;
import static no.unit.nva.cristin.projects.Constants.QueryType.QUERY_USING_GRANT_ID;
import static no.unit.nva.cristin.projects.Constants.QueryType.QUERY_USING_TITLE;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_BACKEND_FAILED_WITH_STATUSCODE;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_QUERY_WITH_PARAMS_FAILED;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_READING_RESPONSE_FAIL;
import static no.unit.nva.cristin.projects.ProjectUriUtils.getNvaProjectUriWithId;
import static no.unit.nva.cristin.projects.ProjectUriUtils.getNvaProjectUriWithParams;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static nva.commons.core.attempt.Try.attempt;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import no.unit.nva.cristin.common.model.SearchResponse;
import no.unit.nva.cristin.projects.Constants.QueryType;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.ProjectSearchResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.attempt.Failure;
import nva.commons.core.attempt.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CristinApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CristinApiClient.class);

    private static final int FIRST_NON_SUCCESS_CODE = 300;

    private final transient HttpClient client;

    public CristinApiClient() {
        this(HttpClient.newHttpClient());
    }

    public CristinApiClient(HttpClient client) {
        this.client = client;
    }

    /**
     * Creates a NvaProject object containing a single transformed Cristin Project. Is used for serialization to the
     * client.
     *
     * @param id       The Cristin id of the project to query
     * @param language Language used for some properties in Cristin API response
     * @return a NvaProject filled with one transformed Cristin Project
     * @throws ApiGatewayException when there is a problem that can be returned to client
     */
    public NvaProject queryOneCristinProjectUsingIdIntoNvaProject(String id, String language)
        throws ApiGatewayException {

        return Optional.of(getProject(id, language))
            .filter(CristinProject::hasValidContent)
            .map(NvaProjectBuilder::new)
            .map(builder -> builder.withContext(PROJECT_LOOKUP_CONTEXT_URL))
            .map(NvaProjectBuilder::build)
            .orElseThrow(() -> projectHasNotValidContent(id));
    }

    /**
     * Creates a wrapper object containing Cristin Projects transformed to NvaProjects with additional metadata. Is used
     * for serialization to the client.
     *
     * @param requestQueryParams Request parameters from client containing title and language
     * @return a ProjectsWrapper filled with transformed Cristin Projects and metadata
     * @throws ApiGatewayException if some error happen we should return this to client
     */
    public SearchResponse queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(
        Map<String, String> requestQueryParams) throws ApiGatewayException {

        long startRequestTime = System.currentTimeMillis();
        QueryType queryType = getQueryTypeBasedOnParams(requestQueryParams);
        HttpResponse<String> response = queryProjects(requestQueryParams, queryType);
        List<CristinProject> cristinProjects =
            getEnrichedProjectsUsingQueryResponse(response, requestQueryParams.get(LANGUAGE));
        if (cristinProjects.isEmpty() && queryType == QUERY_USING_GRANT_ID) {
            response = queryProjects(requestQueryParams, QUERY_USING_TITLE);
            cristinProjects = getEnrichedProjectsUsingQueryResponse(response, requestQueryParams.get(LANGUAGE));
        }
        List<NvaProject> nvaProjects = mapValidCristinProjectsToNvaProjects(cristinProjects);
        long endRequestTime = System.currentTimeMillis();

        ProjectSearchResponse searchResponse = new ProjectSearchResponse();
        searchResponse.withContext(Constants.PROJECT_SEARCH_CONTEXT_URL)
            .usingHeadersAndQueryParams(response.headers(), requestQueryParams)
            .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime));
        searchResponse.withHits(nvaProjects);

        return searchResponse;


        /*return new ProjectSearchResponse()
            .withContext(Constants.PROJECT_SEARCH_CONTEXT_URL)
            .usingHeadersAndQueryParams(response.headers(), requestQueryParams)
            .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime))
            .withHits(nvaProjects);*/
    }

    protected static <T> T fromJson(String body, Class<T> classOfT) throws IOException {
        return OBJECT_MAPPER.readValue(body, classOfT);
    }

    protected HttpResponse<String> queryProjects(Map<String, String> parameters, QueryType queryType)
        throws ApiGatewayException {

        URI uri = attempt(() -> generateQueryProjectsUrl(parameters, queryType))
            .toOptional(failure ->
                logError(ERROR_MESSAGE_QUERY_WITH_PARAMS_FAILED, queryParameters(parameters), failure.getException()))
            .orElseThrow();

        HttpResponse<String> response = fetchQueryResults(uri);

        checkHttpStatusCode(getNvaProjectUriWithParams(parameters).toString(), response.statusCode());

        return response;
    }

    protected CristinProject getProject(String id, String language) throws ApiGatewayException {
        URI uri = attempt(() -> generateGetProjectUri(id, language))
            .toOptional(failure -> logError(ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID, id, failure.getException()))
            .orElseThrow();

        HttpResponse<String> response = fetchGetResult(uri);

        checkHttpStatusCode(getNvaProjectUriWithId(id).toString(), response.statusCode());

        return getDeserializedResponse(response, CristinProject.class);
    }

    protected List<CristinProject> getEnrichedProjectsUsingQueryResponse(HttpResponse<String> response,
                                                                         String language)
        throws ApiGatewayException {

        List<CristinProject> projectsFromQuery = asList(getDeserializedResponse(response, CristinProject[].class));
        List<URI> cristinUris = extractCristinUrisFromProjects(language, projectsFromQuery);
        List<HttpResponse<String>> individualResponses = fetchQueryResultsOneByOne(cristinUris);

        List<CristinProject> enrichedCristinProjects = mapValidResponsesToCristinProjects(individualResponses);

        return allProjectsWereEnriched(projectsFromQuery, enrichedCristinProjects)
            ? enrichedCristinProjects
            : combineResultsWithQueryInCaseEnrichmentFails(projectsFromQuery, enrichedCristinProjects);
    }

    protected List<CristinProject> combineResultsWithQueryInCaseEnrichmentFails(
        List<CristinProject> projectsFromQuery,
        List<CristinProject> enrichedProjects) {

        Set<String> enrichedProjectIds = enrichedProjects.stream()
            .map(CristinProject::getCristinProjectId)
            .collect(Collectors.toSet());

        List<CristinProject> missingProjects = projectsFromQuery.stream()
            .filter(queryProject -> !enrichedProjectIds.contains(queryProject.getCristinProjectId()))
            .collect(Collectors.toList());

        ArrayList<CristinProject> result = new ArrayList<>();
        result.addAll(enrichedProjects);
        result.addAll(missingProjects);
        return result;
    }

    protected List<HttpResponse<String>> fetchQueryResultsOneByOne(List<URI> uris) {
        List<CompletableFuture<HttpResponse<String>>> responsesContainer =
            uris.stream().map(this::fetchGetResultAsync).collect(Collectors.toList());

        return collectSuccessfulResponsesOrThrowException(responsesContainer);
    }

    private boolean allProjectsWereEnriched(List<CristinProject> projectsFromQuery,
                                            List<CristinProject> enrichedCristinProjects) {
        return projectsFromQuery.size() == enrichedCristinProjects.size();
    }

    @JacocoGenerated
    protected CompletableFuture<HttpResponse<String>> fetchGetResultAsync(URI uri) {
        return client.sendAsync(
            HttpRequest.newBuilder(uri).GET().build(),
            BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private List<HttpResponse<String>> collectSuccessfulResponsesOrThrowException(
        List<CompletableFuture<HttpResponse<String>>> responsesContainer) {

        return responsesContainer.stream()
            .map(attempt(CompletableFuture::get))
            .map(Try::orElseThrow)
            .filter(this::isSuccessfulRequest)
            .collect(Collectors.toList());
    }

    protected boolean isSuccessfulRequest(HttpResponse<String> response) {
        try {
            checkHttpStatusCode(nullableUriToString(response.uri()), response.statusCode());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String nullableUriToString(URI uri) throws URISyntaxException {
        return Optional.ofNullable(uri).orElse(new URI(EMPTY_STRING)).toString();
    }

    private List<URI> extractCristinUrisFromProjects(String language, List<CristinProject> projectsFromQuery) {
        return projectsFromQuery.stream()
            .map(attempt(project -> generateGetProjectUri(project.getCristinProjectId(), language)))
            .map(Try::orElseThrow)
            .collect(Collectors.toList());
    }

    private QueryType getQueryTypeBasedOnParams(Map<String, String> requestQueryParams) {
        return Utils.isPositiveInteger(requestQueryParams.get(QUERY)) ? QUERY_USING_GRANT_ID : QUERY_USING_TITLE;
    }

    protected URI generateQueryProjectsUrl(Map<String, String> parameters, QueryType queryType)
        throws URISyntaxException {

        CristinQuery query = new CristinQuery()
            .withLanguage(parameters.get(LANGUAGE))
            .withFromPage(parameters.get(PAGE))
            .withItemsPerPage(parameters.get(NUMBER_OF_RESULTS));

        return queryType == QUERY_USING_GRANT_ID ? query.withGrantId(parameters.get(QUERY)).toURI() :
            query.withTitle(parameters.get(QUERY)).toURI();
    }

    protected URI generateGetProjectUri(String id, String language) throws URISyntaxException {
        return CristinQuery.fromIdAndLanguage(id, language);
    }

    protected long calculateProcessingTime(long startRequestTime, long endRequestTime) {
        return endRequestTime - startRequestTime;
    }

    protected HttpResponse<String> fetchGetResult(URI uri) {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).build();
        return attempt(() -> client.send(httpRequest, BodyHandlers.ofString(StandardCharsets.UTF_8))).orElseThrow();
    }

    private List<CristinProject> mapValidResponsesToCristinProjects(List<HttpResponse<String>> responses) {
        return responses.stream()
            .map(attempt(response -> getDeserializedResponse(response, CristinProject.class)))
            .map(Try::orElseThrow)
            .filter(CristinProject::hasValidContent)
            .collect(Collectors.toList());
    }

    private BadGatewayException projectHasNotValidContent(String id) {
        logger.warn(String.format(ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, id));
        return new BadGatewayException(String.format(ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, id));
    }

    protected HttpResponse<String> fetchQueryResults(URI uri) {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).build();
        return attempt(() -> client.send(httpRequest, BodyHandlers.ofString(StandardCharsets.UTF_8))).orElseThrow();
    }

    private <T> T getDeserializedResponse(HttpResponse<String> response, Class<T> classOfT)
        throws BadGatewayException {

        return attempt(() -> fromJson(response.body(), classOfT))
            .orElseThrow(failure -> logAndThrowDeserializationError(response, failure));
    }

    private <T> BadGatewayException logAndThrowDeserializationError(HttpResponse<String> response, Failure<T> failure) {
        logError(ERROR_MESSAGE_READING_RESPONSE_FAIL, response.body(), failure.getException());
        return new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
    }

    private void checkHttpStatusCode(String uri, int statusCode)
        throws NotFoundException, BadGatewayException {

        if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new NotFoundException(uri);
        } else if (remoteServerHasInternalProblems(statusCode)) {
            logBackendFetchFail(uri, statusCode);
            throw new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
        } else if (errorIsUnknown(statusCode)) {
            logBackendFetchFail(uri, statusCode);
            throw new RuntimeException();
        }
    }

    private boolean errorIsUnknown(int statusCode) {
        return responseIsFailure(statusCode)
            && !remoteServerHasInternalProblems(statusCode);
    }

    private boolean responseIsFailure(int statusCode) {
        return statusCode >= FIRST_NON_SUCCESS_CODE;
    }

    private boolean remoteServerHasInternalProblems(int statusCode) {
        return statusCode >= HttpURLConnection.HTTP_INTERNAL_ERROR;
    }

    private void logBackendFetchFail(String uri, int statusCode) {
        logger.error(String.format(ERROR_MESSAGE_BACKEND_FAILED_WITH_STATUSCODE, statusCode, uri));
    }

    private List<NvaProject> mapValidCristinProjectsToNvaProjects(List<CristinProject> cristinProjects) {
        return cristinProjects.stream()
            .filter(CristinProject::hasValidContent)
            .map(CristinProject::toNvaProject)
            .collect(Collectors.toList());
    }

    private void logError(String message, String data, Exception failure) {
        logger.error(String.format(message, data, failure.getMessage()));
    }
}
