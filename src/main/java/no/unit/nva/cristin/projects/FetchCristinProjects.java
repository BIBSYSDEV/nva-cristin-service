package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Handler for requests to Lambda function.
 */
public class FetchCristinProjects implements RequestHandler<Map<String, Object>, SimpleResponse> {

    public static final String X_CUSTOM_HEADER = "X-Custom-Header";
    public static final String URL_IS_NULL = "The input parameter 'url' is null";
    public static final String ERROR_KEY = "error";
    /**
     * Connection object handling the direct communication via http for (mock)-testing to be injected
     */
    protected transient CristinApiClient cristinApiClient;
    private LambdaLogger logger;

    public FetchCristinProjects() {
        cristinApiClient = new CristinApiClient();
    }

    public FetchCristinProjects(CristinApiClient cristinApiClient) {
        this.cristinApiClient = cristinApiClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SimpleResponse handleRequest(Map<String, Object> input, Context context) {
        logger = context.getLogger();
        Map<String, String> queryStringParameters = (Map<String, String>) input.get("queryStringParameters");
        String title = queryStringParameters.get("title");
        logger.log("Incoming query:" + title + "\n");
        String json;
        int statusCode;
        if (title == null) {
            statusCode = Response.Status.BAD_REQUEST.getStatusCode();
            json = getErrorAsJson(URL_IS_NULL);
        } else {
            try {
                statusCode = Response.Status.OK.getStatusCode();
                Map<String, String> parameters = new TreeMap<>();
                parameters.put("titles", title);
                parameters.put("page", "1");
                parameters.put("per_page", "10");

                List<Project> projects = cristinApiClient.queryProjects(parameters);

                List<Project> enrichedProjects = new ArrayList<>();
                for (Project project : projects) {
                    Project enrichedProject = cristinApiClient.getProject(project.cristin_project_id);
                    enrichedProjects.add(enrichedProject);
                }

                json = new Gson().toJson(enrichedProjects, Project[].class);

            } catch (MalformedURLException | UnsupportedEncodingException e) {
                logger.log(e.toString());
                statusCode = Response.Status.BAD_REQUEST.getStatusCode();
                json = getErrorAsJson(e.getMessage());
            } catch (IOException e) {
                logger.log(e.getMessage());
                statusCode = Response.Status.SERVICE_UNAVAILABLE.getStatusCode();
                json = getErrorAsJson(e.getMessage());
            }
        }
        logger.log("json: " + json + ", statusCode:" + statusCode);
        return new SimpleResponse(json, "" + statusCode);
    }

    /**
     * Get error message as a json string.
     *
     * @param message message from exception
     * @return String containing an error message as json
     */
    protected String getErrorAsJson(String message) {
        JsonObject json = new JsonObject();
        json.addProperty(ERROR_KEY, message);
        return json.toString();
    }

}
