package no.unit.nva.cristin.projects.query.organization;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.projects.CristinQuery.CRISTIN_QUERY_PARAMETER_PARENT_UNIT_ID;
import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;

@SuppressWarnings({"Unused", "UnusedPrivateField"})
public class QueryCristinOrganizationProjectHandler extends CristinQueryHandler<Void, SearchResponse<NvaProject>> {

    public static final Pattern PATTERN = Pattern.compile(ORGANIZATION_IDENTIFIER_PATTERN);
    public static final Set<String> VALID_QUERY_PARAMETERS = Set.of(PAGE, NUMBER_OF_RESULTS);

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

        var requestQueryParameters = Map.of(
                CRISTIN_QUERY_PARAMETER_PARENT_UNIT_ID, getValidId(requestInfo),
                LANGUAGE, getValidLanguage(requestInfo),
                PAGE, getValidPage(requestInfo),
                NUMBER_OF_RESULTS, getValidNumberOfResults(requestInfo));

        return cristinApiClient.listOrganizationProjects(requestQueryParameters);
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
        if (!requestInfo.getPathParameters().containsKey(IDENTIFIER)) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS);
        }
    }

    private String getValidId(RequestInfo requestInfo) throws BadRequestException {
        final var identifier = requestInfo.getPathParameter(IDENTIFIER);
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
}
