package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Handler for requests to Lambda function.
 */
public class FetchCristinProjects implements RequestHandler<Map<String, Object>, GatewayResponse> {

    private static final String QUERY_STRING_PARAMETERS_KEY = "queryStringParameters";
    private static final String TITLE_KEY = "title";
    private static final String LANGUAGE_KEY = "language";

    private static final String TITLE_IS_NULL = "Parameter 'title' is mandatory";
    private static final String TITLE_ILLEGAL_CHARACTERS = "Parameter 'title' may only contain alphanumeric "
            + "characters, dash and whitespace";
    private static final String LANGUAGE_INVALID = "Parameter 'language' has invalid value";

    private static final String EMPTY_STRING = "";
    private static final String DEFAULT_LANGUAGE_CODE = "nb";
    private static final List<String> VALID_LANGUAGE_CODES = Arrays.asList("nb", "en");

    private transient CristinApiClient cristinApiClient;
    private final transient PresentationConverter presentationConverter = new PresentationConverter();

    public FetchCristinProjects() {
        cristinApiClient = new CristinApiClient();
    }

    public FetchCristinProjects(CristinApiClient cristinApiClient) {
        this.cristinApiClient = cristinApiClient;
    }

    public void setCristinApiClient(CristinApiClient cristinApiClient) {
        this.cristinApiClient = cristinApiClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GatewayResponse handleRequest(Map<String, Object> input, Context context) {

        GatewayResponse gatewayResponse = new GatewayResponse();
        try {
            this.checkParameters(input);
        } catch (RuntimeException e) {
            gatewayResponse.setErrorBody(e.getMessage());
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            return gatewayResponse;
        }

        Map<String, String> queryStringParameters = (Map<String, String>) input.get(QUERY_STRING_PARAMETERS_KEY);
        String title = queryStringParameters.get(TITLE_KEY);
        String language = queryStringParameters.getOrDefault(LANGUAGE_KEY, DEFAULT_LANGUAGE_CODE);

        try {
            Map<String, String> parameters = new ConcurrentHashMap<>();
            parameters.put("title", title);
            parameters.put("lang", language);
            parameters.put("page", "1");
            parameters.put("per_page", "10");

            List<Project> projects = cristinApiClient.queryAndEnrichProjects(parameters, language);
            List<ProjectPresentation> projectPresentations = projects.stream()
                    .map(project -> presentationConverter.asProjectPresentation(project, language))
                    .collect(Collectors.toList());

            Type projectListType = new TypeToken<ArrayList<ProjectPresentation>>() {
            }.getType();

            gatewayResponse.setStatusCode(Response.Status.OK.getStatusCode());
            gatewayResponse.setBody(new Gson().toJson(projectPresentations, projectListType));
        } catch (IOException | URISyntaxException e) {
            gatewayResponse.setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            gatewayResponse.setErrorBody(e.getMessage());
        }

        return gatewayResponse;
    }

    @SuppressWarnings("unchecked")
    private void checkParameters(Map<String, Object> input) {
        Map<String, String> queryStringParameters = (Map<String, String>) input.get(QUERY_STRING_PARAMETERS_KEY);
        String title = queryStringParameters.getOrDefault(TITLE_KEY, EMPTY_STRING);
        if (title.isEmpty()) {
            throw new RuntimeException(TITLE_IS_NULL);
        }
        if (!isValidTitle(title)) {
            throw new RuntimeException(TITLE_ILLEGAL_CHARACTERS);
        }

        String language = queryStringParameters.getOrDefault(LANGUAGE_KEY, DEFAULT_LANGUAGE_CODE);
        if (!VALID_LANGUAGE_CODES.contains(language)) {
            throw new RuntimeException(LANGUAGE_INVALID);
        }
    }

    private boolean isValidTitle(String str) {
        char[] charArray = str.toCharArray();
        for (char c : charArray) {
            if (!Character.isWhitespace(c) && !Character.isLetterOrDigit(c) && c != '-') {
                return false;
            }
        }
        return true;
    }

}
