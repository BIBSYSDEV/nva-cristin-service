package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
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
    private static final String URL_IS_NULL = "The input parameter 'url' is null";
    private static final String ERROR_KEY = "error";
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
//        logger = context.getLogger();
        Map<String, String> queryStringParameters = (Map<String, String>) input.get("queryStringParameters");
        String title = queryStringParameters.get("title");
        String language = queryStringParameters.get("language");
        String json;
        int statusCode;
        if (title == null) {
            statusCode = Response.Status.BAD_REQUEST.getStatusCode();
            json = getErrorAsJson(URL_IS_NULL);
        } else {
            try {
                statusCode = Response.Status.OK.getStatusCode();
                Map<String, String> parameters = new TreeMap<>();
                parameters.put("title", title);
                parameters.put("lang", language);
                parameters.put("page", "1");
                parameters.put("per_page", "5");

                List<Project> projects = cristinApiClient.queryProjects(parameters);

                List<ProjectPresentation> projectPresentations = new ArrayList<>();

                for (Project project : projects) {
                    Project enrichedProject = cristinApiClient.getProject(project.cristin_project_id, language);
                    projectPresentations.add(asProjectPresentation(enrichedProject, language));
                }

                Type projectListType = new TypeToken<ArrayList<ProjectPresentation>>() {
                }.getType();
                json = new Gson().toJson(projectPresentations, projectListType);

            } catch (MalformedURLException | UnsupportedEncodingException e) {
//                logger.log(e.toString());
                statusCode = Response.Status.BAD_REQUEST.getStatusCode();
                json = getErrorAsJson(e.getMessage());
            } catch (IOException e) {
//                logger.log(e.getMessage());
                statusCode = Response.Status.SERVICE_UNAVAILABLE.getStatusCode();
                json = getErrorAsJson(e.getMessage());
            }
        }
//        logger.log("json: " + json + ", statusCode:" + statusCode);
        return new SimpleResponse(json, "" + statusCode);
    }

    private ProjectPresentation asProjectPresentation(Project project, String language) {
        ProjectPresentation projectPresentation = new ProjectPresentation();
        projectPresentation.cristin_project_id = project.cristin_project_id;

        for (String key : project.title.keySet()) {
            TitlePresentation titlePresentation = new TitlePresentation();
            titlePresentation.language = key;
            titlePresentation.title = project.title.get(key);
            projectPresentation.titles.add(titlePresentation);
        }

        for (Person person : project.participants) {
            ParticipantPresentation participantPresentation = new ParticipantPresentation();
            participantPresentation.cristin_person_id = person.cristin_person_id;
            participantPresentation.full_name = person.surname + ", " + person.first_name;
            projectPresentation.participants.add(participantPresentation);
        }

        InstitutionPresentation institutionPresentation = new InstitutionPresentation();
        institutionPresentation.cristin_institution_id = project.coordinating_institution.institution.cristin_institution_id;
        institutionPresentation.name = project.coordinating_institution.institution.institution_name.get(language);
        institutionPresentation.language = language;
        projectPresentation.institutions.add(institutionPresentation);

        FundingSourcePresentation fundingSourcePresentation = new FundingSourcePresentation();
        fundingSourcePresentation.funding_source_code = "";
        fundingSourcePresentation.project_code = "";
        projectPresentation.fundings.add(fundingSourcePresentation);

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

}
