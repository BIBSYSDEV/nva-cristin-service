package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessageWithRange;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.cristin.model.JsonPropertyNames.STATUS;
import static nva.commons.core.attempt.Try.attempt;

/**
 * Handler for requests to Lambda function.
 */
public class FetchCristinProjects extends CristinQueryHandler<Void, SearchResponse<NvaProject>> {

    public static final Set<String> VALID_QUERY_PARAMETERS =
            Set.of(QUERY, ORGANIZATION, STATUS, LANGUAGE, PAGE, NUMBER_OF_RESULTS);

    private final transient QueryCristinProjectApiClient cristinApiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public FetchCristinProjects() {
        this(new Environment());
    }

    @JacocoGenerated
    public FetchCristinProjects(Environment environment) {
        this(new QueryCristinProjectApiClient(), environment);
    }

    protected FetchCristinProjects(QueryCristinProjectApiClient cristinApiClient, Environment environment) {
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
        var requestQueryParameters = getQueryParameters(language, query, page, numberOfResults);
        if (requestInfo.getQueryParameters().containsKey(ORGANIZATION)) {
            requestQueryParameters.put(ORGANIZATION, getValidOrganizationUri(requestInfo));
        }
        getValidProjectStatus(requestInfo).ifPresent(status -> requestQueryParameters.put(STATUS, status.name()));
        return getTransformedCristinProjectsUsingWrapperObject(requestQueryParameters);


    }

    @Override
    protected void validateQueryParameterKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMETERS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(validQueryParameterNamesMessage(VALID_QUERY_PARAMETERS));
        }
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

    private Map<String, String> getQueryParameters(String language, String query, String page, String numberOfResults) {
        Map<String, String> requestQueryParameters = new ConcurrentHashMap<>();
        requestQueryParameters.put(LANGUAGE, language);
        requestQueryParameters.put(QUERY, query);
        requestQueryParameters.put(PAGE, page);
        requestQueryParameters.put(NUMBER_OF_RESULTS, numberOfResults);
        return requestQueryParameters;
    }

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

}
