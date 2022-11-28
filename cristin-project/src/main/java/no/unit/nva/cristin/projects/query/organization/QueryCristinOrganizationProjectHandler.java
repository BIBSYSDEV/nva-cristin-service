package no.unit.nva.cristin.projects.query.organization;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.JsonPropertyNames;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.projects.common.CristinQuery.CRISTIN_QUERY_PARAMETER_PARENT_UNIT_ID;
import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;

@SuppressWarnings({"Unused", "UnusedPrivateField"})
public class QueryCristinOrganizationProjectHandler extends CristinQueryHandler<Void, SearchResponse<NvaProject>> {

    public static final Pattern PATTERN = Pattern.compile(ORGANIZATION_IDENTIFIER_PATTERN);
    public static final Set<String> VALID_QUERY_PARAMETERS_VALIDATED = Set.of(JsonPropertyNames.PAGE,
            JsonPropertyNames.NUMBER_OF_RESULTS);

    public static final Set<String> VALID_QUERY_PARAM_NO_VALIDATION =
            Set.of(JsonPropertyNames.CRISTIN_INSTITUTION_ID, JsonPropertyNames.PROJECT_MANAGER,
                    JsonPropertyNames.PROJECT_PARTICIPANT, JsonPropertyNames.PROJECT_KEYWORD,
                    JsonPropertyNames.FUNDING_SOURCE, JsonPropertyNames.FUNDING,
                    JsonPropertyNames.PROJECT_APPROVAL_REFERENCE_ID, JsonPropertyNames.PROJECT_APPROVED_BY,
                    JsonPropertyNames.PROJECT_SORT, JsonPropertyNames.PROJECT_UNIT, JsonPropertyNames.USER,
                    JsonPropertyNames.LEVELS, JsonPropertyNames.BIOBANK_ID);

    public static final Set<String> VALID_QUERY_PARAMETERS = mergeSets(VALID_QUERY_PARAMETERS_VALIDATED,
            VALID_QUERY_PARAM_NO_VALIDATION);

    private final transient QueryCristinOrganizationProjectApiClient cristinApiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public QueryCristinOrganizationProjectHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public QueryCristinOrganizationProjectHandler(Environment environment) {
        this(new QueryCristinOrganizationProjectApiClient(), environment);
    }

    protected QueryCristinOrganizationProjectHandler(QueryCristinOrganizationProjectApiClient cristinApiClient,
                                                     Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = cristinApiClient;
    }


    /**
     * Implements the main logic of the handler. Any exception thrown by this method will be handled by {@link
     * RestRequestHandler#handleExpectedException} method.
     *
     * @param input       The input object to the method. Usually a deserialized json.
     * @param requestInfo Request headers and path.
     * @param context     the ApiGateway context.
     * @return the Response body that is going to be serialized in json
     * @throws ApiGatewayException all exceptions are caught by writeFailure and mapped to error codes through the
     *                             method {@link RestRequestHandler#getFailureStatusCode}
     */
    @Override
    protected SearchResponse<NvaProject> processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        validateHasIdentifierPathParameter(requestInfo);
        validateQueryParameterKeys(requestInfo);

        var requestQueryParameters = extractQueryParameters(requestInfo);

        return cristinApiClient.listOrganizationProjects(requestQueryParameters);
    }

    private Map<String, String> extractQueryParameters(RequestInfo requestInfo) throws BadRequestException {
        var requestQueryParameters = Map.of(
                CRISTIN_QUERY_PARAMETER_PARENT_UNIT_ID, getValidId(requestInfo),
                JsonPropertyNames.LANGUAGE, getValidLanguage(requestInfo),
                JsonPropertyNames.PAGE, getValidPage(requestInfo),
                JsonPropertyNames.NUMBER_OF_RESULTS, getValidNumberOfResults(requestInfo));
        //From here
        VALID_QUERY_PARAM_NO_VALIDATION.forEach(paramName -> putOrNotQueryParameterOrEmpty(requestInfo,
                paramName, requestQueryParameters));
        return requestQueryParameters;
    }

    /**
     * Define the success status code.
     *
     * @param input  The request input.
     * @param output The response output
     * @return the success status code.
     */
    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<NvaProject> output) {
        return HttpURLConnection.HTTP_OK;
    }

    private void validateHasIdentifierPathParameter(RequestInfo requestInfo) throws BadRequestException {
        if (!requestInfo.getPathParameters().containsKey(JsonPropertyNames.IDENTIFIER)) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS);
        }
    }

    private String getValidId(RequestInfo requestInfo) throws BadRequestException {
        final var identifier = requestInfo.getPathParameter(JsonPropertyNames.IDENTIFIER);
        if (PATTERN.matcher(identifier).matches()) {
            return identifier;
        }
        throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS);
    }

    @Override
    protected void validateQueryParameterKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMETERS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(validQueryParameterNamesMessage(VALID_QUERY_PARAMETERS));
        }
    }

    /**
     * Putting the parameter if it exists in a map.
     */
    protected void putOrNotQueryParameterOrEmpty(RequestInfo requestInfo, String parameter,
                                                 Map<String,String> requestQueryParameters) {
        if (getQueryParameter(requestInfo, parameter).isPresent()) {
            requestQueryParameters.put(parameter,getQueryParameter(requestInfo, parameter).get());
        }
    }


    /**
     * Merging 2 sets, even static.
     */
    public static <T> Set<T> mergeSets(Set<T> a, Set<T> b) {
        var mergedSet = new HashSet<T>();
        mergedSet.addAll(a);
        mergedSet.addAll(b);
        return mergedSet;
    }
}