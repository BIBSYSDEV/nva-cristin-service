package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.RestRequestHandler;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.model.Organization.ORGANIZATION_CONTEXT;
import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;
import static no.unit.nva.utils.UriUtils.getNvaApiId;

@SuppressWarnings({"unused","PMD.SingularField"})
public class ListCristinOrganizationPersonsHandler extends CristinQueryHandler<Void, SearchResponse<Person>> {


    public static final Pattern PATTERN = Pattern.compile(ORGANIZATION_IDENTIFIER_PATTERN);
    private static final Logger logger = LoggerFactory.getLogger(FetchCristinPersonHandler.class);
    private final transient ApiClient apiClient;
    private static final Set<String> VALID_QUERY_PARAMS = Set.of(PAGE, NUMBER_OF_RESULTS);
    public static final String ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LIST =
            "Invalid query param supplied. Valid ones are 'page' and 'results'";


    @JacocoGenerated
    public ListCristinOrganizationPersonsHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public ListCristinOrganizationPersonsHandler(Environment environment) {
        this(new ApiClient(HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build()), environment);
    }

    public ListCristinOrganizationPersonsHandler(ApiClient apiClient, Environment environment) {
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
        logger.debug("Will be processing input soon....");
        validateHasIdentifierPathParameter(requestInfo);
        validateQueryParamKeys(requestInfo);
        String identifier = getValidId(requestInfo);
        return new SearchResponse<Person>(getServiceUri(identifier))
                .withHits(Collections.emptyList())
                .withContext(ORGANIZATION_CONTEXT)
                .withSize(0);
    }

    private URI getServiceUri(String identifier) {
        return getNvaApiId(identifier, ORGANIZATION_PATH);
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
    protected void validateQueryParamKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LIST);
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

}
