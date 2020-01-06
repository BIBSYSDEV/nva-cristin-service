package no.unit.nva.cristin.projects;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.jsoup.HttpStatusException;

import javax.ws.rs.BadRequestException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class CristinApiClient {


    private final static String cristinApiURL = "https://api.cristin.no/v2";

    protected String connect(Map<String, String> parameters) throws IOException, BadRequestException {

        URI

        InputStreamReader inputStreamReader = null;
        BufferedReader in = null;
        String contents;
        try {
            URL url = new URL(cristinApiURL);
            if (parameters != null) {
                parameters.keySet().forEach(s -> queryParam(url, s, parameters.get(s)));
            }
            inputStreamReader = this.communicateWith(url);
            in = new BufferedReader(inputStreamReader);
            contents = in.lines().collect(Collectors.joining());
        } finally {
            if (in != null) {
                in.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
        }
        return asList(fromJson(getProjects_json(parameters), Project[].class));
    }

    protected InputStreamReader communicateWith(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        return new InputStreamReader(connection.getInputStream());
    }


    public String getProjects_json(Map<String, String> parameters) throws IOException, BadRequestException {
        try {
            final HttpRequest request = http.get(cristinApiURL + "/projects");
            if (parameters != null) {
                parameters.keySet().forEach(s -> queryParam(request, s, parameters.get(s)));
            }
            return asString(request, 200);
        } catch (HttpStatusException e) {
            switch (e.getStatusCode()) {
                case 400:
                    throw new BadRequestException(e.getMessage(), e);
                default:
                    throw new IOException(e.getMessage(), e);
            }
        }
    }

    static HttpRequest queryParam(URL url, String key, Object value) {
        if (key != null && value != null) {
            url.getQuery().a
            request.queryString(key, value);
        }
        return request;
    }

    static <T> T fromJson(String json, Class<T> classOfT) throws IOException {
        try {
            return new Gson().fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            final String s = e.getMessage() + " " + json;
            throw new IOException(s, e);
        }
    }


}
