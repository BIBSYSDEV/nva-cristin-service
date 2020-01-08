package no.unit.nva.cristin.projects;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.http.client.utils.URIBuilder;

import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class CristinApiClient {

    private static final String HTTPS = "https";
    private static final String CRISTIN_API_HOST = "api.cristin.no";
    private static final String CRISTIN_API_PROJECTS_PATH = "/v2/projects/";

    List<Project> queryProjects(Map<String, String> parameters) throws IOException, URISyntaxException {
        URL url = generateQueryProjectsUrl(parameters);
        try (InputStreamReader streamReader = fetchQueryResults(url)) {
            return asList(fromJson(streamReader, Project[].class));
        }
    }

    Project getProject(String id, String language) throws IOException, URISyntaxException {
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

    protected URL generateQueryProjectsUrl(Map<String, String> parameters) throws MalformedURLException, URISyntaxException {
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

    private static <T> T fromJson(InputStreamReader reader, Class<T> classOfT) throws IOException {
        try {
            return new Gson().fromJson(reader, classOfT);
        } catch (JsonSyntaxException e) {
            final String s = e.getMessage() + " " + reader;
            throw new IOException(s, e);
        }
    }

}
