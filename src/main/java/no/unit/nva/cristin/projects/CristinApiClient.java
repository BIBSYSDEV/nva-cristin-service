package no.unit.nva.cristin.projects;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.projects.Constants.BASE_URL;
import static no.unit.nva.cristin.projects.Constants.LANGUAGE;
import static no.unit.nva.cristin.projects.Constants.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.projects.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.Constants.PAGE;
import static no.unit.nva.cristin.projects.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static no.unit.nva.cristin.projects.Constants.QUERY;
import static no.unit.nva.cristin.projects.Constants.QUESTION_MARK;
import static no.unit.nva.cristin.projects.Constants.QueryType.QUERY_USING_GRANT_ID;
import static no.unit.nva.cristin.projects.Constants.QueryType.QUERY_USING_TITLE;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_BACKEND_FAILED_WITH_STATUSCODE;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_QUERY_WITH_PARAMS_FAILED;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_READING_RESPONSE_FAIL;
import static no.unit.nva.cristin.projects.UriUtils.buildUri;
import static no.unit.nva.cristin.projects.UriUtils.queryParameters;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import no.unit.nva.cristin.projects.Constants.QueryType;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.attempt.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CristinApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CristinApiClient.class);

    private static final int FIRST_NON_SUCCESS_CODE = 300;

    private static final HttpClient client = HttpClient.newHttpClient();

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
    public ProjectsWrapper queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(
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

        return new ProjectsWrapper()
            .usingHeadersAndQueryParams(response.headers(), requestQueryParams)
            .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime))
            .withHits(nvaProjects);
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

        checkHttpStatusCode(
            buildUri(BASE_URL, QUESTION_MARK + queryParameters(parameters)).toString(),
            response.statusCode());

        return response;
    }

    protected CristinProject getProject(String id, String language) throws ApiGatewayException {
        URI uri = attempt(() -> generateGetProjectUri(id, language))
            .toOptional(failure -> logError(ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID, id, failure.getException()))
            .orElseThrow();

        HttpResponse<String> response = fetchGetResult(uri);

        checkHttpStatusCode(buildUri(BASE_URL, id).toString(), response.statusCode());

        return getDeserializedResponse(response, CristinProject.class);
    }

    protected List<CristinProject> getEnrichedProjectsUsingQueryResponse(HttpResponse<String> response,
                                                                         String language)
        throws ApiGatewayException {

        List<CristinProject> projectsFromQuery = asList(getDeserializedResponse(response, CristinProject[].class));
        List<URI> cristinUris = extractCristinUrisFromProjects(language, projectsFromQuery);
        List<HttpResponse<String>> individualResponses = fetchQueryResultsOneByOne(cristinUris);
        List<CristinProject> enrichedCristinProjects = mapValidResponsesToCristinProjects(individualResponses);
        enrichedCristinProjects =
            combineResultsWithQueryInCaseEnrichmentFails(projectsFromQuery, enrichedCristinProjects);

        return enrichedCristinProjects;

        /*return projectsFromQuery.stream()
            .map(project -> attempt(() -> getProject(project.getCristinProjectId(), language))
                .toOptional(failure -> logError(
                    ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID,
                    project.getCristinProjectId(),
                    failure.getException()))
                .orElse(project)).collect(Collectors.toList());*/
    }

    protected List<CristinProject> combineResultsWithQueryInCaseEnrichmentFails(
        List<CristinProject> projectsFromQuery,
        List<CristinProject> enrichedProjects) {

        if (projectsFromQuery.size() == enrichedProjects.size()) {
            return enrichedProjects;
        }

        List<CristinProject> missingProjects = projectsFromQuery.stream()
            .filter(queryProject -> enrichedProjects.stream().noneMatch(
                enrichedProject -> enrichedProject.getCristinProjectId().equals(queryProject.getCristinProjectId())))
            .collect(Collectors.toList());

        enrichedProjects.addAll(missingProjects);

        return enrichedProjects;
    }

    @JacocoGenerated
    protected List<HttpResponse<String>> fetchQueryResultsOneByOne(List<URI> uris) {
        List<CompletableFuture<HttpResponse<String>>> responsesContainer =
            uris.stream().map(uri ->
                client.sendAsync(
                    HttpRequest.newBuilder(uri).GET().build(),
                    BodyHandlers.ofString(StandardCharsets.UTF_8)))
                .collect(Collectors.toList());

        // TODO: Warning message if enrich fail just as before?
        return responsesContainer.stream()
            .map(attempt(CompletableFuture::get))
            .map(Try::get)
            .collect(Collectors.toList());
    }

    private List<URI> extractCristinUrisFromProjects(String language, List<CristinProject> projectsFromQuery) {
        return projectsFromQuery.stream()
            .map(project -> attempt(() -> generateGetProjectUri(project.getCristinProjectId(), language))
                .orElseThrow())
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

    @JacocoGenerated
    protected long calculateProcessingTime(long startRequestTime, long endRequestTime) {
        return endRequestTime - startRequestTime;
    }

    @JacocoGenerated
    protected HttpResponse<String> fetchGetResult(URI uri) {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).build();

        return attempt(() -> client.send(httpRequest, BodyHandlers.ofString(StandardCharsets.UTF_8))).orElseThrow();
    }

    private List<CristinProject> mapValidResponsesToCristinProjects(List<HttpResponse<String>> responses) {
        return responses.stream()
            .map(response -> attempt(() -> getDeserializedResponse(response, CristinProject.class)).get())
            .filter(CristinProject::hasValidContent)
            .collect(Collectors.toList());
    }

    private BadGatewayException projectHasNotValidContent(String id) {
        logger.warn(String.format(ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, id));
        return new BadGatewayException(String.format(ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, id));
    }

    @JacocoGenerated
    protected HttpResponse<String> fetchQueryResults(URI uri) {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).build();

        return attempt(() -> client.send(httpRequest, BodyHandlers.ofString(StandardCharsets.UTF_8))).orElseThrow();
    }

    private <T> T getDeserializedResponse(HttpResponse<String> response, Class<T> classOfT)
        throws BadGatewayException {

        return attempt(() -> fromJson(response.body(), classOfT)).orElseThrow(failure -> {
            logError(ERROR_MESSAGE_READING_RESPONSE_FAIL, response.body(),
                failure.getException());
            return new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
        });
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
