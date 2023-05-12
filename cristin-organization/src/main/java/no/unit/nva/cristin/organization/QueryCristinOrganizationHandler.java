package no.unit.nva.cristin.organization;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.ErrorMessages;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.Constants.FULL;
import static no.unit.nva.cristin.model.Constants.NONE;
import static no.unit.nva.cristin.model.Constants.TOP;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.cristin.organization.DefaultOrgQueryClientProvider.VERSION;

public class QueryCristinOrganizationHandler extends CristinQueryHandler<Void, SearchResponse<Organization>> {

    private static final Set<String> VALID_QUERY_PARAMETERS = Set.of(QUERY, PAGE, NUMBER_OF_RESULTS, DEPTH, VERSION);
    private final transient IClientProvider<RequestInfo, IQueryApiClient<Organization>> clientProvider;

    @JacocoGenerated
    @SuppressWarnings("unused")
    public QueryCristinOrganizationHandler() {
        this(new DefaultOrgQueryClientProvider(), new Environment());
    }

    public QueryCristinOrganizationHandler(IClientProvider<RequestInfo, IQueryApiClient<Organization>> clientProvider,
                                           Environment environment) {
        super(Void.class, environment);
        this.clientProvider = clientProvider;
    }

    @Override
    protected SearchResponse<Organization> processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        validateQueryParameterKeys(requestInfo);
        Map<String, String> requestQueryParams = new ConcurrentHashMap<>();
        requestQueryParams.put(QUERY, getValidQuery(requestInfo));
        requestQueryParams.put(DEPTH, getValidDepth(requestInfo));
        requestQueryParams.put(PAGE, getValidPage(requestInfo));
        requestQueryParams.put(NUMBER_OF_RESULTS, getValidNumberOfResults(requestInfo));

        return clientProvider.getClient(requestInfo).executeQuery(requestQueryParams);
    }

    @Override
    protected void validateQueryParameterKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMETERS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(validQueryParameterNamesMessage(VALID_QUERY_PARAMETERS));
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<Organization> output) {
        return HttpURLConnection.HTTP_OK;
    }

    protected static String getValidDepth(RequestInfo requestInfo) throws BadRequestException {
        if (isValidDepth(requestInfo)) {
            return requestInfo.getQueryParameters().containsKey(DEPTH)
                    ? requestInfo.getQueryParameter(DEPTH)
                    : TOP;
        } else {
            throw new BadRequestException(ErrorMessages.ERROR_MESSAGE_DEPTH_INVALID);
        }
    }

    private static boolean isValidDepth(RequestInfo requestInfo) throws BadRequestException {
        return !requestInfo.getQueryParameters().containsKey(DEPTH)
                || requestInfo.getQueryParameters().containsKey(DEPTH)
                && Set.of(TOP, FULL, NONE).contains(requestInfo.getQueryParameter(DEPTH));
    }

}
