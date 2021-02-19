package no.unit.nva.cristin.projects;

import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import org.slf4j.LoggerFactory;

/**
 * Handler for requests to Lambda function.
 */
public class FetchCristinProjects extends ApiGatewayHandler<Void, ProjectPresentation[]> {

    private static final String CRISTIN_QUERY_PARAMETER_TITLE_KEY = "title";
    private static final String CRISTIN_QUERY_PARAMETER_LANGUAGE_KEY = "lang";
    private static final String CRISTIN_QUERY_PARAMETER_PAGE_KEY = "page";
    private static final String CRISTIN_QUERY_PARAMETER_PAGE_VALUE = "1";
    private static final String CRISTIN_QUERY_PARAMETER_PER_PAGE_KEY = "per_page";
    private static final String CRISTIN_QUERY_PARAMETER_PER_PAGE_VALUE = "5";

    public static final String MISSING_QUERY_PARAMETER_ERROR_MESSAGE = "Missing mandatory query parameter: ";

    public static final String LANGUAGE_QUERY_PARAMETER = "language";
    public static final String TITLE_QUERY_PARAMETER = "title";
    private static final String DEFAULT_LANGUAGE_CODE = "nb";
    private final transient CristinApiClient cristinApiClient;
    private final transient PresentationConverter presentationConverter = new PresentationConverter();

    public FetchCristinProjects(CristinApiClient cristinApiClient, Environment environment) {
        super(Void.class, environment, LoggerFactory.getLogger(FetchCristinProjects.class));
        this.cristinApiClient = cristinApiClient;
    }

    @Override
    protected ProjectPresentation[] processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        String language = getQueryParameterOrDefault(requestInfo, LANGUAGE_QUERY_PARAMETER, DEFAULT_LANGUAGE_CODE);
        String title = getQueryParameter(requestInfo, TITLE_QUERY_PARAMETER);

        return createProjectPresentations(language, title);
    }

    private String getQueryParameter(RequestInfo requestInfo, String queryParameter)
        throws BadRequestException {
        return attempt(() -> requestInfo.getQueryParameter(queryParameter))
            .orElseThrow(failure -> handleMissingParameter(queryParameter));
    }

    private String getQueryParameterOrDefault(RequestInfo requestInfo, String queryParameter, String defaultValue) {
        return attempt(() -> requestInfo.getQueryParameter(queryParameter))
            .orElse(failure -> defaultValue);
    }

    private BadRequestException handleMissingParameter(String queryParameterName) {
        return new BadRequestException(MISSING_QUERY_PARAMETER_ERROR_MESSAGE + queryParameterName);
    }

    private ProjectPresentation[] createProjectPresentations(String language, String title) {
        List<ProjectPresentation> projectPresentations = createProjectPresentationList(language, title);
        return convertToArray(projectPresentations);
    }

    private ProjectPresentation[] convertToArray(List<ProjectPresentation> projectPresentations) {
        ProjectPresentation[] projectPresentationsArray = new ProjectPresentation[projectPresentations.size()];
        projectPresentations.toArray(projectPresentationsArray);
        return projectPresentationsArray;
    }

    private List<ProjectPresentation> createProjectPresentationList(String language, String title) {
        Map<String, String> cristinQueryParameters = createCristinQueryParameters(title, language);
        List<Project> projects = attempt(()-> cristinApiClient.queryAndEnrichProjects(cristinQueryParameters, language)).orElseThrow();

        return projects.stream()
            .map(project -> presentationConverter.asProjectPresentation(project, language))
            .collect(Collectors.toList());
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, ProjectPresentation[] output) {
        return HttpURLConnection.HTTP_OK;
    }


    private Map<String, String> createCristinQueryParameters(String title, String language) {
        Map<String, String> queryParameters = new ConcurrentHashMap<>();
        queryParameters.put(CRISTIN_QUERY_PARAMETER_TITLE_KEY, title);
        queryParameters.put(CRISTIN_QUERY_PARAMETER_LANGUAGE_KEY, language);
        queryParameters.put(CRISTIN_QUERY_PARAMETER_PAGE_KEY, CRISTIN_QUERY_PARAMETER_PAGE_VALUE);
        queryParameters.put(CRISTIN_QUERY_PARAMETER_PER_PAGE_KEY, CRISTIN_QUERY_PARAMETER_PER_PAGE_VALUE);
        return queryParameters;
    }

}
