package no.unit.nva.cristin.projects;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.projects.Constants.BASE_URL;
import static no.unit.nva.cristin.projects.Constants.CRISTIN_API_HOST;
import static no.unit.nva.cristin.projects.UriUtils.buildUri;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonUtils;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CristinApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CristinApiClient.class);

    private static final String TITLE = "title";
    private static final String CHARACTER_EQUALS = "=";
    private static final String HTTPS = "https";
    private static final String CRISTIN_API_PROJECTS_PATH = "/v2/projects/";
    private static final ObjectMapper OBJECT_MAPPER = JsonUtils.objectMapper;
    private static final String SEARCH_PATH = "search?QUERY_PARAMS"; // TODO: NP-2412: Replace QUERY_PARAMS
    private static final String ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID =
        "Error fetching cristin project with id: %s . Exception Message: %s";

    protected static <T> T fromJson(InputStreamReader reader, Class<T> classOfT) throws IOException {
        return OBJECT_MAPPER.readValue(reader, classOfT);
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
        projectsWrapper.setNextResults(null);
        projectsWrapper.setHits(transformCristinProjectsToNvaProjects(enrichedProjects));

        // TODO: Return fields with empty values instead of null to avoid "undefined" in frontend
        return projectsWrapper;
    }

    /**
     * Creates a NvaProject object containing a single transformed Cristin Project. Is used for serialization to the
     * client.
     *
     * @param id       The Cristin id of the project to query
     * @param language Language used for some properties in Cristin API response
     * @return a NvaProject filled with one transformed Cristin Project
     */
    public NvaProject queryOneCristinProjectUsingIdIntoNvaProject(String id, String language) {

        CristinProject cristinProject = attemptToGetCristinProject(id, language);

        if (cristinProject == null || cristinProject.cristinProjectId == null) {
            return new NvaProject();
        }

        NvaProject nvaProject = new NvaProjectBuilder(cristinProject).build();
        nvaProject.setContext(Constants.PROJECT_LOOKUP_CONTEXT_URL);

        return nvaProject;
    }

    private CristinProject attemptToGetCristinProject(String id, String language) {
        return attempt(() -> getProject(id, language))
            .orElse(failure -> {
                logger.error(String.format(ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID,
                    id, failure.getException().getMessage()));
                return null;
            });
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
            .map(cristinProject -> new NvaProjectBuilder(cristinProject).build())
            .collect(Collectors.toList());
    }

    protected List<CristinProject> queryProjects(Map<String, String> parameters) throws IOException,
                                                                                        URISyntaxException {
        URL url = generateQueryProjectsUrl(parameters);
        try (InputStreamReader streamReader = fetchQueryResults(url)) {
            return asList(fromJson(streamReader, CristinProject[].class));
        }
    }

    protected List<CristinProject> queryAndEnrichProjects(Map<String, String> parameters,
                                                          String language) throws IOException,
                                                                                  URISyntaxException {
        List<CristinProject> projects = queryProjects(parameters);
        return enrichProjects(language, projects);
    }

    protected CristinProject getProject(String id, String language) throws IOException, URISyntaxException {
        URL url = generateGetProjectUrl(id, language);
        try (InputStreamReader streamReader = fetchGetResult(url)) {
            return fromJson(streamReader, CristinProject.class);
        }
    }

    @JacocoGenerated
    protected InputStreamReader fetchQueryResults(URL url) throws IOException {
        return new InputStreamReader(url.openStream());
    }

    @JacocoGenerated
    protected InputStreamReader fetchGetResult(URL url) throws IOException {
        return new InputStreamReader(url.openStream());
    }

    protected URL generateQueryProjectsUrl(Map<String, String> parameters) throws MalformedURLException,
                                                                                  URISyntaxException {
        URIBuilder uri = new URIBuilder()
            .setScheme(HTTPS)
            .setHost(CRISTIN_API_HOST)
            .setPath(CRISTIN_API_PROJECTS_PATH);
        if (parameters != null) {
            parameters.keySet().forEach(s -> uri.addParameter(s, parameters.get(s)));
        }
        return uri.build().toURL();
    }

    protected URL generateGetProjectUrl(String id, String language) throws MalformedURLException, URISyntaxException {
        URI uri = new URIBuilder()
            .setScheme(HTTPS)
            .setHost(CRISTIN_API_HOST)
            .setPath(CRISTIN_API_PROJECTS_PATH + id)
            .addParameter("lang", language)
            .build();
        return uri.toURL();
    }

    private List<CristinProject> enrichProjects(String language, List<CristinProject> projects) {
        return projects.stream().map(project -> enrichOneProject(language, project)).collect(Collectors.toList());
    }

    private CristinProject enrichOneProject(String language, CristinProject project) {
        return attempt(() -> getProject(project.cristinProjectId, language))
            .orElse((failure) -> {
                logger.error(String.format(ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID,
                    project.cristinProjectId, failure.getException().getMessage()));
                return project;
            });
    }
}