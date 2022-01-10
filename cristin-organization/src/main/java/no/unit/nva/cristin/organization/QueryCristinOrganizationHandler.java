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

import static no.unit.nva.cristin.model.Constants.FULL;
import static no.unit.nva.cristin.model.Constants.TOP;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static nva.commons.core.attempt.Try.attempt;

public class QueryCristinOrganizationHandler extends CristinQueryHandler<Void, SearchResponse<Organization>> {

    private final transient CristinOrganizationApiClient cristinApiClient;
    private static final Set<String> VALID_QUERY_PARAMS = Set.of(QUERY, PAGE, NUMBER_OF_RESULTS, DEPTH);


    @JacocoGenerated
    public QueryCristinOrganizationHandler() {
        this(new CristinOrganizationApiClient(), new Environment());
    }

    public QueryCristinOrganizationHandler(CristinOrganizationApiClient apiClient, Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = apiClient;
    }

    @Override
    protected SearchResponse<Organization> processInput(Void input, RequestInfo requestInfo, Context context)
            throws ApiGatewayException {

        validateQueryParamKeys(requestInfo);
        Map<String, String> requestQueryParams = new ConcurrentHashMap<>();
        requestQueryParams.put(QUERY, getValidQuery(requestInfo));
        requestQueryParams.put(DEPTH, getValidDepth(requestInfo));
        requestQueryParams.put(PAGE, getValidPage(requestInfo));
        requestQueryParams.put(NUMBER_OF_RESULTS, getValidNumberOfResults(requestInfo));
        return attempt(() -> cristinApiClient.queryOrganizations(requestQueryParams)).orElseThrow();
    }

    @Override
    protected void validateQueryParamKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_SEARCH);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<Organization> output) {
        return HttpURLConnection.HTTP_OK;
    }

    private String getValidDepth(RequestInfo requestInfo) throws BadRequestException {
        if (isValidDepth(requestInfo)) {
            return requestInfo.getQueryParameters().containsKey(DEPTH)
                    ? requestInfo.getQueryParameter(DEPTH)
                    : TOP;
        } else {
            throw new BadRequestException(ErrorMessages.ERROR_MESSAGE_DEPTH_INVALID);
        }
    }

    private boolean isValidDepth(RequestInfo requestInfo) throws BadRequestException {
        return !requestInfo.getQueryParameters().containsKey(DEPTH)
                || requestInfo.getQueryParameters().containsKey(DEPTH)
                && Set.of(TOP, FULL).contains(requestInfo.getQueryParameter(DEPTH));
    }


}
