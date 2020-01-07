package no.unit.nva.cristin.projects;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.http.client.utils.URIBuilder;

import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class CristinApiClient {


    List<Project> queryProjects(Map<String, String> parameters) throws IOException, BadRequestException {
        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme("https")
                    .setHost("api.cristin.no")
                    .setPath("/v2/projects");

            if (parameters != null) {
                parameters.keySet().forEach(s -> builder.addParameter(s, parameters.get(s)));
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(builder.build())
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();
            return asList(fromJson(responseBody, Project[].class));
        } catch (URISyntaxException | InterruptedException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    Project getProject(String id, String language) throws IOException, BadRequestException {
        try {
            URIBuilder builder = new URIBuilder()
                    .setScheme("https")
                    .setHost("api.cristin.no")
                    .setPath("/v2/projects/" + id)
                    .addParameter("lang", language);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(builder.build())
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();
            return fromJson(responseBody, Project.class);
        } catch (URISyntaxException | InterruptedException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }


    private static <T> T fromJson(String json, Class<T> classOfT) throws IOException {
        try {
            return new Gson().fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            final String s = e.getMessage() + " " + json;
            throw new IOException(s, e);
        }
    }


}
