package no.unit.nva.cristin.projects;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.projects.CommonUtil.buildUri;
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
    // TODO: Replace ENVIRONMENT and QUERY_PARAMS with real values
    //  and remember to dynamically mock when writing unit tests
    private static final String API_SEARCH_URL_WITH_PARAMS =
        "https://api.ENVIRONMENT.nva.aws.unit.no/project/search?QUERY_PARAMS";
    private static final String ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID = "Error fetching cristin project with"
        + " id: ";
    private static final String CHARACTER_PUNCTUATION = ".";
    private static final String EXCEPTION_MESSAGE_PREFIX = " Exception message: ";

    protected static <T> T fromJson(InputStreamReader reader, Class<T> classOfT) throws IOException {
        return OBJECT_MAPPER.readValue(reader, classOfT);
    }

    /**
     * Creates a wrapper object containing Cristin Projects transformed to NvaProjects with additional metadata. Is used
     * used for serialization to the client.
     *
     * @param parameters The query params
     * @param language   Language used for some properties in Cristin API response
     * @return a ProjectsWrapper filled with transformed Cristin Projects and metadata
     * @throws IOException        if cannot read from connection
     * @throws URISyntaxException if URI is malformed
     */
    public ProjectsWrapper createProjectsWrapperFromQuery(Map<String, String> parameters, String language)
        throws IOException, URISyntaxException {

        long startRequestTime = System.currentTimeMillis();
        List<CristinProject> enrichedProjects = queryAndEnrichProjects(parameters, language);
        long endRequestTime = System.currentTimeMillis();

        ProjectsWrapper projectsWrapper = new ProjectsWrapper();

        projectsWrapper.setId(buildUri(API_SEARCH_URL_WITH_PARAMS));
        projectsWrapper.setSize(0); // TODO: X-Total-Count header from Cristin response
        projectsWrapper.setSearchString(extractTitleSearchString(parameters));
        projectsWrapper.setProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime));
        // TODO: Use Link header / Pagination data from Cristin response in the next two values
        projectsWrapper.setFirstRecord(0);
        projectsWrapper.setNextResults(null);
        projectsWrapper.setHits(transformCristinProjectsToNvaProjects(enrichedProjects));

        return projectsWrapper;
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
            .map(NvaProjectBuilder::mapCristinProjectToNvaProject)
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
        List<CristinProject> enrichedProjects = enrichProjects(language, projects);
        return enrichedProjects;
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
            .setHost(Constants.CRISTIN_API_HOST)
            .setPath(CRISTIN_API_PROJECTS_PATH);
        if (parameters != null) {
            parameters.keySet().forEach(s -> uri.addParameter(s, parameters.get(s)));
        }
        return uri.build().toURL();
    }

    protected URL generateGetProjectUrl(String id, String language) throws MalformedURLException, URISyntaxException {
        URI uri = new URIBuilder()
            .setScheme(HTTPS)
            .setHost(Constants.CRISTIN_API_HOST)
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
                logger.error(ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID
                    + project.cristinProjectId
                    + CHARACTER_PUNCTUATION
                    + EXCEPTION_MESSAGE_PREFIX
                    + failure.getException().getMessage());
                return project;
            });
    }
}