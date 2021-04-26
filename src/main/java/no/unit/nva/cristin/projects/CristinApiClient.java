package no.unit.nva.cristin.projects;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.projects.Constants.BASE_URL;
import static no.unit.nva.cristin.projects.Constants.CRISTIN_API_HOST;
import static no.unit.nva.cristin.projects.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.UriUtils.buildUri;
import static nva.commons.core.attempt.Try.attempt;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.core.JacocoGenerated;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CristinApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CristinApiClient.class);

    private static final String TITLE = "title";
    private static final String CHARACTER_EQUALS = "=";
    private static final String HTTPS = "https";
    private static final String CRISTIN_API_PROJECTS_PATH = "/v2/projects/";
    private static final String SEARCH_PATH = "search?QUERY_PARAMS"; // TODO: NP-2412: Replace QUERY_PARAMS
    private static final String ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID =
        "Error fetching cristin project with id: %s . Exception Message: %s";
    private static final String ERROR_MESSAGE_BACKEND_FETCH_FAILED =
        "Your request cannot be processed at this time due to an upstream error";
    private static final String ERROR_MESSAGE_NOT_FOUND =
        "The requested resource %s does not exist";
    private static final String ERROR_MESSAGE_INTERNAL_ERROR =
        "Your request cannot be processed at this time because of an internal server error";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final int STATUS_CODE_NOT_FOUND = 404;
    private static final int STATUS_CODE_START_OF_ERROR_CODES_RANGE = 299;
    private static final String CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID =
        "Project matching id %s does not have valid data";

    protected static <T> T fromJson(InputStreamReader reader, Class<T> classOfT) throws IOException {
        return OBJECT_MAPPER.readValue(reader, classOfT);
    }

    protected static <T> T fromJson(InputStream stream, Class<T> classOfT) throws IOException {
        return fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), classOfT);
    }

    /**
     * Creates a NvaProject object containing a single transformed Cristin Project. Is used for serialization to the
     * client.
     *
     * @param id       The Cristin id of the project to query
     * @param language Language used for some properties in Cristin API response
     * @return a NvaProject filled with one transformed Cristin Project
     * @throws BadGatewayException          when there is a problem with fetch from backend
     * @throws InternalServerErrorException when there is an unforeseen error
     * @throws NotFoundException            when upstream returns 404
     */
    public NvaProject queryOneCristinProjectUsingIdIntoNvaProject(String id, String language)
        throws BadGatewayException, InternalServerErrorException, NotFoundException {

        CristinProject cristinProject = attemptToGetCristinProject(id, language);

        if (cristinProject == null || !cristinProject.hasValidContent()) {
            logger.warn(String.format(CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, id));
            throw new BadGatewayException(String.format(CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID, id));
            //return new EmptyNvaProject(); // TODO: Remove if unneeded
        }

        NvaProject nvaProject = new NvaProjectBuilder(cristinProject).build();
        nvaProject.setContext(Constants.PROJECT_LOOKUP_CONTEXT_URL);

        return nvaProject;
    }

    /**
     * Creates a wrapper object containing Cristin Projects transformed to NvaProjects with additional metadata. Is used
     * for serialization to the client.
     *
     * @param parameters The query params
     * @param language   Language used for some properties in Cristin API response
     * @return a ProjectsWrapper filled with transformed Cristin Projects and metadata
     * @throws IOException        if cannot read from connection
     * @throws URISyntaxException if URI is malformed
     */
    public ProjectsWrapper queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(
        Map<String, String> parameters, String language)
        throws IOException, URISyntaxException {

        long startRequestTime = System.currentTimeMillis();
        List<CristinProject> enrichedProjects = queryAndEnrichProjects(parameters, language);
        long endRequestTime = System.currentTimeMillis();

        ProjectsWrapper projectsWrapper = new ProjectsWrapper();

        projectsWrapper.setId(buildUri(BASE_URL, SEARCH_PATH));
        projectsWrapper.setSize(0); // TODO: NP-2385: X-Total-Count header from Cristin response
        projectsWrapper.setSearchString(extractTitleSearchString(parameters));
        projectsWrapper.setProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime));
        // TODO: NP-2385: Use Link header / Pagination data from Cristin response in the next two values
        projectsWrapper.setFirstRecord(0);
        projectsWrapper.setNextResults(null); // TODO: Change to URI
        projectsWrapper.setHits(transformCristinProjectsToNvaProjects(enrichedProjects));

        return projectsWrapper;
    }

    // TODO: throw BadGatewayException if this fails as well?
    protected List<CristinProject> queryProjects(Map<String, String> parameters) throws IOException,
                                                                                        URISyntaxException {
        URI uri = generateQueryProjectsUrl(parameters);
        var response = fetchQueryResults(uri);

        return asList(fromJson(response.body(), CristinProject[].class));
    }

    @JacocoGenerated
    protected long calculateProcessingTime(long startRequestTime, long endRequestTime) {
        return endRequestTime - startRequestTime;
    }

    private String extractTitleSearchString(Map<String, String> parameters) {
        return TITLE + CHARACTER_EQUALS + parameters.get(TITLE);
    }

    private List<NvaProject> transformCristinProjectsToNvaProjects(List<CristinProject> cristinProjects) {
        return cristinProjects.stream()
            .filter(CristinProject::hasValidContent)
            .map(cristinProject -> new NvaProjectBuilder(cristinProject).build())
            .collect(Collectors.toList());
    }

    protected CristinProject getProject(String id, String language)
        throws IOException, URISyntaxException, NotFoundException, BadGatewayException {

        URI uri = generateGetProjectUri(id, language);
        HttpResponse<InputStream> response = fetchGetResult(uri);

        if (response.statusCode() == STATUS_CODE_NOT_FOUND) {
            // TODO: NP-2315: Use full Nva resource path instead of only id
            throw new NotFoundException(String.format(ERROR_MESSAGE_NOT_FOUND, id));
        }

        if (response.statusCode() > STATUS_CODE_START_OF_ERROR_CODES_RANGE) {
            throw new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
        }

        return fromJson(response.body(), CristinProject.class);
    }

    protected List<CristinProject> queryAndEnrichProjects(Map<String, String> parameters,
                                                          String language) throws IOException,
                                                                                  URISyntaxException {
        List<CristinProject> projects = queryProjects(parameters);
        return enrichProjects(language, projects);
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

    protected URI generateGetProjectUri(String id, String language) throws URISyntaxException {
        return new URIBuilder() // TODO: Replace URIBuilder() with only URI and put logic in UriUtils?
            .setScheme(HTTPS)
            .setHost(CRISTIN_API_HOST)
            .setPath(CRISTIN_API_PROJECTS_PATH + id)
            .addParameter("lang", language)
            .build();
    }

    protected URI generateQueryProjectsUrl(Map<String, String> parameters) throws URISyntaxException {
        URIBuilder uri = new URIBuilder()
            .setScheme(HTTPS)
            .setHost(CRISTIN_API_HOST)
            .setPath(CRISTIN_API_PROJECTS_PATH);
        if (parameters != null) {
            parameters.keySet().forEach(s -> uri.addParameter(s, parameters.get(s)));
        }
        return uri.build();
    }

    private CristinProject attemptToGetCristinProject(String id, String language)
        throws BadGatewayException, NotFoundException, InternalServerErrorException {

        try {
            return getProject(id, language);
            // TODO: NP-2437: Should these be thrown as other than InternalServerErrorException?
        } catch (URISyntaxException exception) {
            logError(id, exception);
            throw new InternalServerErrorException(ERROR_MESSAGE_INTERNAL_ERROR);
        } catch (IOException exception) {
            logError(id, exception);
            throw new BadGatewayException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
        }
    }

    private List<CristinProject> enrichProjects(String language, List<CristinProject> projects) {
        return projects.stream().map(project -> enrichOneProject(language, project)).collect(Collectors.toList());
    }

    private CristinProject enrichOneProject(String language, CristinProject project) {
        return attempt(() -> getProject(project.getCristinProjectId(), language))
            .toOptional(failure -> logError(project.getCristinProjectId(), failure.getException()))
            .orElse(project);
    }

    private void logError(String id, Exception failure) {
        logger.error(String.format(ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID,
            id, failure.getMessage()));
    }

}
