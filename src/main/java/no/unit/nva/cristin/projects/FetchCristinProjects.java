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
import java.util.*;

/**
 * Handler for requests to Lambda function.
 */
public class FetchCristinProjects implements RequestHandler<Map<String, Object>, GatewayResponse> {

    private static final String TITLE_IS_NULL = "Parameter 'title' is mandatory";
    private static final String TITLE_ILLEGAL_CHARACTERS = "Parameter 'title' contains non-alphanumeric characters";
    private static final String LANGUAGE_INVALID = "Parameter 'language' has invalid value";
    private static final String ERROR_KEY = "error";
    private static final String DEFAULT_LANGUAGE_CODE = "nb";
    private static final List<String> VALID_LANGUAGE_CODES = Arrays.asList("nb", "en");


    private CristinApiClient cristinApiClient;

    public FetchCristinProjects(CristinApiClient cristinApiClient) {
        this.cristinApiClient = cristinApiClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GatewayResponse handleRequest(Map<String, Object> input, Context context) {

        GatewayResponse gatewayResponse = new GatewayResponse();
        Map<String, String> queryStringParameters = (Map<String, String>) input.get("queryStringParameters");
        String title = queryStringParameters.getOrDefault("title", "");
        String language = queryStringParameters.getOrDefault("language", DEFAULT_LANGUAGE_CODE);

        if (title.isEmpty()) {
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            gatewayResponse.setBody(getErrorAsJson(TITLE_IS_NULL));
            return gatewayResponse;
        }

        if (!isAlphanumeric(title)) {
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            gatewayResponse.setBody(getErrorAsJson(TITLE_ILLEGAL_CHARACTERS));
            return gatewayResponse;
        }

        if (!VALID_LANGUAGE_CODES.contains(language)) {
            gatewayResponse.setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
            gatewayResponse.setBody(getErrorAsJson(LANGUAGE_INVALID));
            return gatewayResponse;
        }

        try {
            Map<String, String> parameters = new TreeMap<>();
            parameters.put("title", title);
            parameters.put("lang", language);
            parameters.put("page", "1");
            parameters.put("per_page", "5");

            List<Project> projects = cristinApiClient.queryProjects(parameters);
            List<ProjectPresentation> projectPresentations = new ArrayList<>();

            for (Project project : projects) {
                Project enrichedProject = cristinApiClient.getProject(project.getCristin_project_id(), language);
                projectPresentations.add(asProjectPresentation(enrichedProject, language));
            }

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
        projectPresentation.cristin_project_id = project.getCristin_project_id();

        for (String key : project.getTitle().keySet()) {
            TitlePresentation titlePresentation = new TitlePresentation();
            titlePresentation.language = key;
            titlePresentation.title = project.getTitle().get(key);
            projectPresentation.titles.add(titlePresentation);
        }

        for (Person person : project.getParticipants()) {
            ParticipantPresentation participantPresentation = new ParticipantPresentation();
            participantPresentation.cristin_person_id = person.getCristin_person_id();
            participantPresentation.full_name = person.getSurname() + ", " + person.getFirst_name();
            projectPresentation.participants.add(participantPresentation);
        }

        InstitutionPresentation institutionPresentation = new InstitutionPresentation();
        institutionPresentation.cristin_institution_id = project.getCoordinating_institution().getInstitution().getCristin_institution_id();
        institutionPresentation.name = project.getCoordinating_institution().getInstitution().getInstitution_name().get(language);
        institutionPresentation.language = language;
        projectPresentation.institutions.add(institutionPresentation);

        for (FundingSource fundingSource : project.getProject_funding_sources()) {
            FundingSourcePresentation fundingSourcePresentation = new FundingSourcePresentation();
            fundingSourcePresentation.funding_source_code = fundingSource.getFunding_source_code();
            fundingSourcePresentation.project_code = fundingSource.getProject_code();
            projectPresentation.fundings.add(fundingSourcePresentation);
        }

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

    private boolean isAlphanumeric(String str) {
        char[] charArray = str.toCharArray();
        for (char c : charArray) {
            if (!Character.isLetterOrDigit(c))
                return false;
        }
        return true;
    }

}
