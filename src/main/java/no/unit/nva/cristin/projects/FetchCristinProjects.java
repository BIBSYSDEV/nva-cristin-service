package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Handler for requests to Lambda function.
 */
public class FetchCristinProjects implements RequestHandler<Map<String, Object>, GatewayResponse> {

    private static final String TITLE_IS_NULL = "Parameter 'title' is mandatory";
    private static final String TITLE_ILLEGAL_CHARACTERS = "Parameter 'title' may only contain alphanumeric "
            + "characters, dash and whitespace";
    private static final String LANGUAGE_INVALID = "Parameter 'language' has invalid value";
    private static final String ERROR_KEY = "error";
    private static final String DEFAULT_LANGUAGE_CODE = "nb";
    private static final List<String> VALID_LANGUAGE_CODES = Arrays.asList("nb", "en");

    private transient CristinApiClient cristinApiClient;

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
        Map<String, String> queryStringParameters = (Map<String, String>) input.get("queryStringParameters");
        String title = queryStringParameters.getOrDefault("title", "");
        if (title.isEmpty()) {
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            gatewayResponse.setBody(getErrorAsJson(TITLE_IS_NULL));
            return gatewayResponse;
        }
        if (!isValidTitle(title)) {
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            gatewayResponse.setBody(getErrorAsJson(TITLE_ILLEGAL_CHARACTERS));
            return gatewayResponse;
        }

        String language = queryStringParameters.getOrDefault("language", DEFAULT_LANGUAGE_CODE);
        if (!VALID_LANGUAGE_CODES.contains(language)) {
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            gatewayResponse.setBody(getErrorAsJson(LANGUAGE_INVALID));
            return gatewayResponse;
        }

        try {
            Map<String, String> parameters = new ConcurrentHashMap<>();
            parameters.put("title", title);
            parameters.put("lang", language);
            parameters.put("page", "1");
            parameters.put("per_page", "5");

            List<Project> projects = cristinApiClient.queryProjects(parameters);
            List<ProjectPresentation> projectPresentations = projects.stream()
                    .map(project -> {
                        try {
                            return cristinApiClient.getProject(project.cristinProjectId, language);
                        } catch (IOException | URISyntaxException e) {
                            System.out.println("Error fetching cristin project with id: " + project.cristinProjectId);
                        }
                        return project;
                    })
                    .map(project -> asProjectPresentation(project, language))
                    .collect(Collectors.toList());

            Type projectListType = new TypeToken<ArrayList<ProjectPresentation>>() {
            }.getType();

            gatewayResponse.setStatusCode(Response.Status.OK.getStatusCode());
            gatewayResponse.setBody(new Gson().toJson(projectPresentations, projectListType));
        } catch (IOException | URISyntaxException e) {
            gatewayResponse.setStatusCode(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
            gatewayResponse.setBody(getErrorAsJson(e.getMessage()));
        }

        return gatewayResponse;
    }

    private ProjectPresentation asProjectPresentation(Project project, String language) {
        ProjectPresentation projectPresentation = new ProjectPresentation();
        projectPresentation.cristinProjectId = project.cristinProjectId;

        Optional.ofNullable(project.title).orElse(new TreeMap<String, String>() {
        }).forEach((key, value) -> {
            TitlePresentation titlePresentation = new TitlePresentation();
            titlePresentation.language = key;
            titlePresentation.title = value;
            projectPresentation.titles.add(titlePresentation);
        });

        Optional.ofNullable(project.participants).orElse(new ArrayList<>()).forEach(person -> {
            ParticipantPresentation participantPresentation = new ParticipantPresentation();
            participantPresentation.cristinPersonId = person.cristinPersonId;
            participantPresentation.fullName = person.surname + ", " + person.firstName;
            projectPresentation.participants.add(participantPresentation);
        });

        InstitutionPresentation institutionPresentation = new InstitutionPresentation();
        if (Optional.ofNullable(project.coordinatingInstitution).isPresent()) {
            institutionPresentation.cristinInstitutionId = project.coordinatingInstitution.institution
                    .cristinInstitutionId;
            institutionPresentation.name = project.coordinatingInstitution.institution.institutionName
                    .get(language);
            institutionPresentation.language = language;
            projectPresentation.institutions.add(institutionPresentation);
        }

        Optional.ofNullable(project.projectFundingSources).orElse(new ArrayList<>()).forEach(fundingSource -> {
            FundingSourcePresentation fundingSourcePresentation = new FundingSourcePresentation();
            fundingSourcePresentation.fundingSourceCode = fundingSource.fundingSourceCode;
            fundingSourcePresentation.projectCode = fundingSource.projectCode;
            projectPresentation.fundings.add(fundingSourcePresentation);
        });

        return projectPresentation;
    }

    /**
     * Get error message as a json string.
     *
     * @param message message from exception
     * @return String containing an error message as json
     */
    private String getErrorAsJson(String message) {
        JsonObject json = new JsonObject();
        json.addProperty(ERROR_KEY, message);
        return json.toString();
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
