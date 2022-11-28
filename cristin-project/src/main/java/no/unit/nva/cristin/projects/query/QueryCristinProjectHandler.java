package no.unit.nva.cristin.projects.query;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.JsonPropertyNames;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessageWithRange;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.Constants.FIRST_PAGE;
import static no.unit.nva.cristin.model.Constants.UNIT_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.*;
import static nva.commons.core.attempt.Try.attempt;

/**
 * Handler for requests to Lambda function.
 */
public class QueryCristinProjectHandler extends CristinQueryHandler<Void, SearchResponse<NvaProject>> {

    public static final Set<String> VALID_QUERY_PARAMETERS_VALIDATED =
            Set.of(QUERY, ORGANIZATION, STATUS, LANGUAGE, PAGE, NUMBER_OF_RESULTS);

    public static final Set<String> VALID_QUERY_PARAM_NO_VALIDATION =
            Set.of(CRISTIN_INSTITUTION_ID, PROJECT_MANAGER, PROJECT_PARTICIPANT,
                    PROJECT_KEYWORD, FUNDING_SOURCE, FUNDING, PROJECT_APPROVAL_REFERENCE_ID,
                    PROJECT_APPROVED_BY, PROJECT_SORT, JsonPropertyNames.PROJECT_UNIT, USER, LEVELS, BIOBANK_ID);

    public static final Set<String> VALID_QUERY_PARAMETERS = mergeSets(VALID_QUERY_PARAMETERS_VALIDATED, VALID_QUERY_PARAM_NO_VALIDATION);

    private final transient QueryCristinProjectApiClient cristinApiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public QueryCristinProjectHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public QueryCristinProjectHandler(Environment environment) {
        this(new QueryCristinProjectApiClient(), environment);
    }

    protected QueryCristinProjectHandler(QueryCristinProjectApiClient cristinApiClient, Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = cristinApiClient;
    }

    @Override
    protected SearchResponse<NvaProject> processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        validateQueryParameterKeys(requestInfo);

        var language = getValidLanguage(requestInfo);
        var query = getValidQuery(requestInfo);
        var page = getValidPage(requestInfo);
        var numberOfResults = getValidNumberOfResults(requestInfo);
        var requestQueryParameters = extractQueryParameters(requestInfo);
        if (requestInfo.getQueryParameters().containsKey(ORGANIZATION)) {
            requestQueryParameters.put(ORGANIZATION, getValidOrganizationUri(requestInfo));
        }
        getValidProjectStatus(requestInfo).ifPresent(status -> requestQueryParameters.put(STATUS, status.name()));
        return getTransformedCristinProjectsUsingWrapperObject(requestQueryParameters);


    }

    private Map<String, String> extractQueryParameters(RequestInfo requestInfo) throws BadRequestException {
        Map<String, String> requestQueryParameters = new ConcurrentHashMap<>();
        requestQueryParameters.put(LANGUAGE, getValidLanguage(requestInfo));
        requestQueryParameters.put(QUERY, getValidQuery(requestInfo));
        requestQueryParameters.put(PAGE, getValidPage(requestInfo));
        requestQueryParameters.put(NUMBER_OF_RESULTS, getValidNumberOfResults(requestInfo));
        //From here
        VALID_QUERY_PARAM_NO_VALIDATION.forEach(paramName -> putOrNotQueryParameterOrEmpty(requestInfo, paramName, requestQueryParameters));
        return requestQueryParameters;
    }

    @Override
    protected void validateQueryParameterKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMETERS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(validQueryParameterNamesMessage(VALID_QUERY_PARAMETERS));
        }
    }

    protected void putOrNotQueryParameterOrEmpty (RequestInfo requestInfo, String parameter, Map<String,String> requestQueryParameters) {
        if(getQueryParameter(requestInfo, parameter).isPresent()){
            requestQueryParameters.put(parameter,getQueryParameter(requestInfo, parameter).get());
        };
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse output) {
        return HttpURLConnection.HTTP_OK;
    }

    private SearchResponse<NvaProject> getTransformedCristinProjectsUsingWrapperObject(
            Map<String, String> requestQueryParameters)
            throws ApiGatewayException {
        return cristinApiClient.queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(requestQueryParameters);
    }
//TODO: remove?
//    private Map<String, String> getQueryParameters(String language, String query, String page, String numberOfResults) {
//        Map<String, String> requestQueryParameters = new ConcurrentHashMap<>();
//        requestQueryParameters.put(LANGUAGE, language);
//        requestQueryParameters.put(QUERY, query);
//        requestQueryParameters.put(PAGE, page);
//        requestQueryParameters.put(NUMBER_OF_RESULTS, numberOfResults);
//        return requestQueryParameters;
//    }
//
    private Optional<ProjectStatus> getValidProjectStatus(RequestInfo requestInfo) throws BadRequestException {
        return requestInfo.getQueryParameters().containsKey(STATUS)
                ? getOptionalProjectStatus(requestInfo)
                : Optional.empty();
    }

    private Optional<ProjectStatus> getOptionalProjectStatus(RequestInfo requestInfo) throws BadRequestException {
        return Optional.ofNullable(
                attempt(() -> ProjectStatus.getNvaStatus(requestInfo.getQueryParameters().get(STATUS)))
                .orElseThrow(fail -> new BadRequestException(getInvalidStatusMessage())));
    }

    private String getInvalidStatusMessage() {
        return invalidQueryParametersMessageWithRange(STATUS, Arrays.toString(ProjectStatus.values()));
    }

    public static<T> Set<T> mergeSets(Set<T> a, Set<T> b)
    {
        return new HashSet<T>() {{
            addAll(a);
            addAll(b);
        }};
    }


}
