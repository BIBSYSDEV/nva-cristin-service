package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.client.CristinOrganizationPersonsClient;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParametersMessage;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;

public class ListCristinOrganizationPersonsHandler extends CristinQueryHandler<Void, SearchResponse<Person>> {

    public static final Pattern PATTERN = Pattern.compile(ORGANIZATION_IDENTIFIER_PATTERN);
    public static final Set<String> VALID_QUERY_PARAMETERS = Set.of(PAGE, NUMBER_OF_RESULTS);
    private final transient CristinOrganizationPersonsClient apiClient;

    @JacocoGenerated
    public ListCristinOrganizationPersonsHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public ListCristinOrganizationPersonsHandler(Environment environment) {
        this(new CristinOrganizationPersonsClient(), environment);
    }

    public ListCristinOrganizationPersonsHandler(CristinOrganizationPersonsClient apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
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
    protected SearchResponse<Person> processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        validateHasIdentifierPathParameter(requestInfo);
        validateQueryParameterKeys(requestInfo);

        String identifier = getValidId(requestInfo);
        String page = getValidPage(requestInfo);
        String numberOfResults = getValidNumberOfResults(requestInfo);
        Map<String, String> requestQueryParams = buildParamMap(identifier, page, numberOfResults);

        return apiClient.generateQueryResponse(requestQueryParams);
    }

    /**
     * Define the success status code.
     *
     * @param input  The request input.
     * @param output The response output
     * @return the success status code.
     */
    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<Person> output) {
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    protected void validateQueryParameterKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMETERS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(validQueryParametersMessage(VALID_QUERY_PARAMETERS));
        }
    }

    private void validateHasIdentifierPathParameter(RequestInfo requestInfo) throws BadRequestException {
        if (!requestInfo.getPathParameters().containsKey(IDENTIFIER)) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS);
        }
    }

    private String getValidId(RequestInfo requestInfo) throws BadRequestException {
        final String identifier = requestInfo.getPathParameter(IDENTIFIER);
        if (PATTERN.matcher(identifier).matches()) {
            return identifier;
        }
        throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS);
    }

    private Map<String, String> buildParamMap(String identifier, String page, String numberOfResults) {
        Map<String, String> requestQueryParameters = new ConcurrentHashMap<>();
        requestQueryParameters.put(IDENTIFIER, identifier);
        requestQueryParameters.put(PAGE, page);
        requestQueryParameters.put(NUMBER_OF_RESULTS, numberOfResults);
        return requestQueryParameters;
    }
}
