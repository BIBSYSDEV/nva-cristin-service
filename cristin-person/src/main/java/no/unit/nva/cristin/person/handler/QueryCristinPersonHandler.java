package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;
import static nva.commons.core.attempt.Try.attempt;

public class QueryCristinPersonHandler extends CristinQueryHandler<Void, SearchResponse<Person>> {

    private static final Logger logger = LoggerFactory.getLogger(QueryCristinPersonHandler.class);

    private static final Set<String> VALID_QUERY_PARAMETERS = Set.of(NAME, ORGANIZATION,  PAGE, NUMBER_OF_RESULTS);
    private final transient CristinPersonApiClient apiClient;

    @JacocoGenerated
    @SuppressWarnings("unused")
    public QueryCristinPersonHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public QueryCristinPersonHandler(Environment environment) {
        this(new CristinPersonApiClient(), environment);
    }

    public QueryCristinPersonHandler(CristinPersonApiClient apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected SearchResponse<Person> processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        validateQueryParameterKeys(requestInfo);

        var name = getValidName(requestInfo);
        var organization = getValidOrganization(requestInfo);
        var page = getValidPage(requestInfo);
        var numberOfResults = getValidNumberOfResults(requestInfo);

        var requestQueryParameters = buildParametersMap(name, page, numberOfResults);
        organization.ifPresent(presentOrganization -> requestQueryParameters.put(ORGANIZATION, presentOrganization));

        return attempt(() -> getAuthorizedSearchResponse(requestInfo, requestQueryParameters))
                   .orElse(list -> apiClient.generateQueryResponse(requestQueryParameters));
    }

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


    private Map<String, String> buildParametersMap(String name, String page, String numberOfResults) {
        var requestQueryParameters = new ConcurrentHashMap<String, String>();
        requestQueryParameters.put(NAME, name);
        requestQueryParameters.put(PAGE, page);
        requestQueryParameters.put(NUMBER_OF_RESULTS, numberOfResults);
        return requestQueryParameters;
    }

    private SearchResponse<Person> getAuthorizedSearchResponse(RequestInfo requestInfo,
                                                               Map<String, String> requestQueryParams)
        throws ApiGatewayException {
        AccessUtils.validateIdentificationNumberAccess(requestInfo);
        logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));
        return apiClient.authorizedGenerateQueryResponse(requestQueryParams);
    }

}
