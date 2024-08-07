package no.unit.nva.cristin.person.query.organization;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cognito.TokenValidator;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.Constants.SORT;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;
import static no.unit.nva.utils.AccessUtils.clientIsCustomerAdministrator;
import static no.unit.nva.utils.AccessUtils.requesterIsUserAdministrator;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;

public class ListCristinOrganizationPersonsHandler extends CristinQueryHandler<Void, SearchResponse<Person>> {

    private static final Logger logger = LoggerFactory.getLogger(ListCristinOrganizationPersonsHandler.class);

    public static final Pattern PATTERN = Pattern.compile(ORGANIZATION_IDENTIFIER_PATTERN);
    public static final Set<String> VALID_QUERY_PARAMETERS = Set.of(PAGE, NUMBER_OF_RESULTS, NAME, SORT);
    public static final String IS_DOING_AUTHORIZED_REQUEST = "Client is doing authorized request";
    public static final String TOKEN_BUT_DID_NOT_HAVE_ANY_ACCESS_RIGHTS =
        "Client has token but did not have any access rights";
    private final transient CristinOrganizationPersonsClient apiClient;

    @JacocoGenerated
    @SuppressWarnings("unused")
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

    @Override
    protected void validateRequest(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateHasIdentifierPathParameter(requestInfo);
        validateQueryParameterKeys(requestInfo);
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

        var requestQueryParams = extractQueryParams(requestInfo);
        logWhenMissingAccessRights(requestInfo);

        if (clientIsAuthorized(requestInfo)) {
            logger.info(IS_DOING_AUTHORIZED_REQUEST);
            logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));
            return apiClient.authorizedGenerateQueryResponse(requestQueryParams);
        } else {
            return apiClient.generateQueryResponse(requestQueryParams);
        }
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
            throw new BadRequestException(validQueryParameterNamesMessage(VALID_QUERY_PARAMETERS));
        }
    }

    private void validateHasIdentifierPathParameter(RequestInfo requestInfo) throws BadRequestException {
        if (!requestInfo.getPathParameters().containsKey(IDENTIFIER)) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS);
        }
    }

    private Map<String, String> extractQueryParams(RequestInfo requestInfo) throws BadRequestException {
        var identifier = getValidId(requestInfo);
        var page = getValidPage(requestInfo);
        var numberOfResults = getValidNumberOfResults(requestInfo);
        var name = getNameIfPresent(requestInfo).orElse(null);
        var sort = getSortIfPresent(requestInfo).orElse(null);

        return buildParamMap(identifier, page, numberOfResults, name, sort);
    }

    private String getValidId(RequestInfo requestInfo) throws BadRequestException {
        final String identifier = requestInfo.getPathParameter(IDENTIFIER);
        if (PATTERN.matcher(identifier).matches()) {
            return identifier;
        }
        throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS);
    }

    private Optional<String> getNameIfPresent(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(NAME);
    }

    private Optional<String> getSortIfPresent(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(SORT);
    }

    private Map<String, String> buildParamMap(String identifier, String page, String numberOfResults, String name,
                                              String sort) {
        Map<String, String> requestQueryParameters = new ConcurrentHashMap<>();
        requestQueryParameters.put(IDENTIFIER, identifier);
        requestQueryParameters.put(PAGE, page);
        requestQueryParameters.put(NUMBER_OF_RESULTS, numberOfResults);
        if (nonNull(name)) {
            requestQueryParameters.put(NAME, name);
        }
        if (nonNull(sort)) {
            requestQueryParameters.put(SORT, sort);
        }
        return requestQueryParameters;
    }

    private void logWhenMissingAccessRights(RequestInfo requestInfo) {
        var tokenValidator = new TokenValidator(requestInfo);
        if (tokenValidator.hasTokenButNoAccessRights()) {
            logger.info(TOKEN_BUT_DID_NOT_HAVE_ANY_ACCESS_RIGHTS);
            logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));
        }
    }

    private boolean clientIsAuthorized(RequestInfo requestInfo) {
        return requesterIsUserAdministrator(requestInfo) || clientIsCustomerAdministrator(requestInfo);
    }

}
