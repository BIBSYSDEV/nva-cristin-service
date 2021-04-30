package no.unit.nva.cristin.projects;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.projects.Constants.BASE_URL;
import static no.unit.nva.cristin.projects.Constants.CRISTIN_API_HOST;
import static no.unit.nva.cristin.projects.Constants.CRISTIN_LANGUAGE_PARAM;
import static no.unit.nva.cristin.projects.Constants.LANGUAGE;
import static no.unit.nva.cristin.projects.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.Constants.TITLE;
import static no.unit.nva.cristin.projects.UriUtils.buildUri;
import static no.unit.nva.cristin.projects.UriUtils.queryParameters;
import static nva.commons.core.attempt.Try.attempt;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CristinApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CristinApiClient.class);

    private static final String HTTPS = "https";
    private static final String CRISTIN_API_PROJECTS_PATH = "/v2/projects/";
    private static final String EMPTY_FRAGMENT = null;
    private static final String ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID =
        "Error fetching cristin project with id: %s . Exception Message: %s";
    private static final String ERROR_MESSAGE_BACKEND_FETCH_FAILED =
        "Your request cannot be processed at this time due to an upstream error";
    private static final String ERROR_MESSAGE_NOT_FOUND =
        "The requested resource %s does not exist";
    private static final int STATUS_CODE_NOT_FOUND = 404;
    private static final int STATUS_CODE_START_OF_ERROR_CODES_RANGE = 299;
    private static final String CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID =
        "Project matching id %s does not have valid data";
    private static final String ERROR_MESSAGE_QUERY_WITH_PARAMS_FAILED =
        "Query failed from params: %s with exception: %s";
    private static final String QUESTION_MARK = "?";

    private static final String CRISTIN_QUERY_PARAMETER_TITLE_KEY = "title";
    private static final String CRISTIN_QUERY_PARAMETER_LANGUAGE_KEY = "lang";
    private static final String CRISTIN_QUERY_PARAMETER_PAGE_KEY = "page";
    private static final String CRISTIN_QUERY_PARAMETER_PAGE_VALUE = "1";
    private static final String CRISTIN_QUERY_PARAMETER_PER_PAGE_KEY = "per_page";
    private static final String CRISTIN_QUERY_PARAMETER_PER_PAGE_VALUE = "5";

    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Creates a NvaProject object containing a single transformed Cristin Project. Is used for serialization to the
     * client.
     *
     * @param id       The Cristin id of the project to query
     * @param language Language used for some properties in Cristin API response
     * @return a NvaProject filled with one transformed Cristin Project
     * @throws BadGatewayException          when there is a problem with fetch from backend
     * @throws NotFoundException            when upstream returns 404
     */
    public NvaProject queryOneCristinProjectUsingIdIntoNvaProject(String id, String language)
        throws BadGatewayException, NotFoundException {

        return attemptToGetCristinProject(id, language)
            .filter(CristinProject::hasValidContent)
            .map(NvaProjectBuilder::new)
            .map(builder -> builder.withContext(Constants.PROJECT_LOOKUP_CONTEXT_URL))
            .map(NvaProjectBuilder::build)
            .orElseThrow(() -> projectHasNotValidContent(id));
    }

    protected static <T> T fromJson(InputStream stream, Class<T> classOfT) throws IOException {
        return OBJECT_MAPPER.readValue(stream, classOfT);
    }

    private BadGatewayException projectHasNotValidContent(String id) {
        logger.warn(String.format(CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, id));
        return new BadGatewayException(String.format(CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, id));
    }

    /**
     * Creates a wrapper object containing Cristin Projects transformed to NvaProjects with additional metadata. Is used
     * for serialization to the client.
     *
     * @param requestQueryParams Request parameters from client containing title and language
     * @return a ProjectsWrapper filled with transformed Cristin Projects and metadata
     * @throws IOException if cannot read from connection
     */
    public ProjectsWrapper queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(
        Map<String, String> requestQueryParams) throws IOException {

        long startRequestTime = System.currentTimeMillis();
        List<NvaProject> nvaProjects = importProjectsFromCristin(requestQueryParams);
        long endRequestTime = System.currentTimeMillis();

        ProjectsWrapper projectsWrapper = new ProjectsWrapper();

        projectsWrapper.setId(buildUri(BASE_URL, QUESTION_MARK + queryParameters(requestQueryParams)));
        projectsWrapper.setSize(0); // TODO: NP-2385: X-Total-Count header from Cristin response
        projectsWrapper.setSearchString(queryParameters(requestQueryParams));
        projectsWrapper.setProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime));
        // TODO: NP-2385: Use Link header / Pagination data from Cristin response in the next two values
        projectsWrapper.setFirstRecord(0);
        projectsWrapper.setNextResults(null); // TODO: Change to URI
        projectsWrapper.setHits(nvaProjects);

        return projectsWrapper;
    }

    private List<NvaProject> importProjectsFromCristin(Map<String, String> requestQueryParams)
        throws IOException {

        return queryAndEnrichProjects(requestQueryParams).stream()
            .filter(CristinProject::hasValidContent)
            .map(CristinProject::toNvaProject)
            .collect(Collectors.toList());
    }

    // TODO: throw BadGatewayException if this fails as well?
    protected List<CristinProject> queryProjects(Map<String, String> parameters) throws IOException {
        URI uri = generateQueryProjectsUrl(parameters);
        var response = fetchQueryResults(uri);

        return asList(fromJson(response.body(), CristinProject[].class));
    }

    @JacocoGenerated
    protected long calculateProcessingTime(long startRequestTime, long endRequestTime) {
        return endRequestTime - startRequestTime;
    }

    protected Optional<CristinProject> getProject(String id, String language)
        throws IOException, NotFoundException, BadGatewayException {

        URI uri = generateGetProjectUri(id, language);
        HttpResponse<InputStream> response = fetchGetResult(uri);

        if (response.statusCode() == STATUS_CODE_NOT_FOUND) {
            // TODO: NP-2315: Use full Nva resource path instead of only id
            throw new NotFoundException(String.format(ERROR_MESSAGE_NOT_FOUND, id));
        }

        if (response.statusCode() > STATUS_CODE_START_OF_ERROR_CODES_RANGE) {
            throw new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
        }

        return Optional.ofNullable(fromJson(response.body(), CristinProject.class));
    }

    protected List<CristinProject> queryAndEnrichProjects(Map<String, String> requestQueryParams) throws IOException {
        List<CristinProject> projects = queryProjects(cristinQueryParamsFromRequestQueryParams(requestQueryParams));
        return enrichProjects(requestQueryParams.get(LANGUAGE), projects);
    }

    @JacocoGenerated
    protected HttpResponse<InputStream> fetchGetResult(URI uri) {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).build();

        return attempt(() -> client.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream())).orElseThrow();
    }

    @JacocoGenerated
    protected HttpResponse<InputStream> fetchQueryResults(URI uri) {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).build();

        return attempt(() -> client.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream())).orElseThrow();
    }

    protected URI generateGetProjectUri(String id, String language) {
        String query = queryParameters(Map.of(CRISTIN_LANGUAGE_PARAM, language));
        return attempt(() ->
            new URI(HTTPS, CRISTIN_API_HOST, CRISTIN_API_PROJECTS_PATH + id, query, EMPTY_FRAGMENT))
            .toOptional(failure -> logError(id, failure.getException())).orElseThrow();
    }

    protected URI generateQueryProjectsUrl(Map<String, String> parameters) {
        String query = queryParameters(parameters);
        return attempt(() ->
            new URI(HTTPS, CRISTIN_API_HOST, CRISTIN_API_PROJECTS_PATH, query, EMPTY_FRAGMENT))
            .toOptional(failure -> logQueryError(query, failure.getException())).orElseThrow();
    }

    private Optional<CristinProject> attemptToGetCristinProject(String id, String language)
        throws BadGatewayException, NotFoundException {

        try {
            return getProject(id, language);
        } catch (IOException exception) {
            logError(id, exception);
            throw new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
        }
    }

    private List<CristinProject> enrichProjects(String language, List<CristinProject> projects) {
        return projects.stream().map(project -> enrichOneProject(language, project)
            .orElse(project)).collect(Collectors.toList());
    }

    private Optional<CristinProject> enrichOneProject(String language, CristinProject project) {
        return attempt(() -> getProject(project.getCristinProjectId(), language))
            .toOptional(failure -> logError(project.getCristinProjectId(), failure.getException()))
            .orElse(Optional.empty());
    }

    private void logError(String id, Exception failure) {
        logger.error(String.format(ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID,
            id, failure.getMessage()));
    }

    private void logQueryError(String queryParams, Exception failure) {
        logger.error(String.format(ERROR_MESSAGE_QUERY_WITH_PARAMS_FAILED,
            queryParams, failure.getMessage()));
    }

    private Map<String, String> cristinQueryParamsFromRequestQueryParams(Map<String, String> requestParams) {
        Map<String, String> cristinQueryParameters = new ConcurrentHashMap<>();
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_TITLE_KEY, requestParams.get(TITLE));
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_LANGUAGE_KEY, requestParams.get(LANGUAGE));
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_PAGE_KEY, CRISTIN_QUERY_PARAMETER_PAGE_VALUE);
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_PER_PAGE_KEY, CRISTIN_QUERY_PARAMETER_PER_PAGE_VALUE);
        return cristinQueryParameters;
    }
}
