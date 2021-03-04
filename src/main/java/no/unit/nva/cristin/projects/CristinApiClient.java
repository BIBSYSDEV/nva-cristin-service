package no.unit.nva.cristin.projects;

import static java.util.Arrays.asList;
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
import nva.commons.core.JsonUtils;
import org.apache.http.client.utils.URIBuilder;

public class CristinApiClient {

    private static final String HTTPS = "https";
    private static final String CRISTIN_API_PROJECTS_PATH = "/v2/projects/";
    private static final ObjectMapper OBJECT_MAPPER = JsonUtils.objectMapper;
    private final transient String cristinApiHost;

    public CristinApiClient(String cristinApiHost) {
        this.cristinApiHost = cristinApiHost;
    }

    protected static <T> T fromJson(InputStreamReader reader, Class<T> classOfT) throws IOException {
        return OBJECT_MAPPER.readValue(reader, classOfT);
    }

    protected List<Project> queryProjects(Map<String, String> parameters) throws IOException, URISyntaxException {
        URL url = generateQueryProjectsUrl(parameters);
        try (InputStreamReader streamReader = fetchQueryResults(url)) {
            return asList(fromJson(streamReader, Project[].class));
        }
    }

    protected List<Project> queryAndEnrichProjects(Map<String, String> parameters, String language) throws
                                                                                                    IOException,
                                                                                                    URISyntaxException {
        List<Project> projects = queryProjects(parameters);
        List<Project> enrichedProjects = enrichProjects(language, projects);
        return enrichedProjects;
    }

    protected Project getProject(String id, String language) throws IOException, URISyntaxException {
        URL url = generateGetProjectUrl(id, language);
        try (InputStreamReader streamReader = fetchGetResult(url)) {
            return fromJson(streamReader, Project.class);
        }
    }

    protected InputStreamReader fetchQueryResults(URL url) throws IOException {
        return new InputStreamReader(url.openStream());
    }

    protected InputStreamReader fetchGetResult(URL url) throws IOException {
        return new InputStreamReader(url.openStream());
    }

    protected URL generateQueryProjectsUrl(Map<String, String> parameters) throws MalformedURLException,
                                                                                  URISyntaxException {
        URIBuilder uri = new URIBuilder()
            .setScheme(HTTPS)
            .setHost(cristinApiHost)
            .setPath(CRISTIN_API_PROJECTS_PATH);
        if (parameters != null) {
            parameters.keySet().forEach(s -> uri.addParameter(s, parameters.get(s)));
        }
        return uri.build().toURL();
    }

    protected URL generateGetProjectUrl(String id, String language) throws MalformedURLException, URISyntaxException {
        URI uri = new URIBuilder()
            .setScheme(HTTPS)
            .setHost(cristinApiHost)
            .setPath(CRISTIN_API_PROJECTS_PATH + id)
            .addParameter("lang", language)
            .build();
        return uri.toURL();
    }

    private List<Project> enrichProjects(String language, List<Project> projects) {
        return projects.stream().map(project -> enrichOneProject(language, project)).collect(Collectors.toList());
    }

    private Project enrichOneProject(String language, Project project) {
        return attempt(() -> getProject(project.cristinProjectId, language))
            .orElse((failure) -> {
                System.out.println("Error fetching cristin project with id: " + project.cristinProjectId);
                return project;
            });
    }
}