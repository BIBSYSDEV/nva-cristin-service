package no.unit.nva.cristin.organization.query;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Optional;
import no.unit.nva.client.ClientProvider;
import no.unit.nva.cristin.common.client.CristinQueryApiClient;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.UriUtils;
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
import static no.unit.nva.client.ClientProvider.VERSION;
import static no.unit.nva.cristin.model.Constants.SORT;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.cristin.organization.common.HandlerUtil.getValidDepth;
import static no.unit.nva.utils.VersioningUtils.ACCEPT_HEADER_KEY_NAME;
import static no.unit.nva.utils.VersioningUtils.extractVersionFromRequestInfo;

public class QueryCristinOrganizationHandler extends CristinQueryHandler<Void, SearchResponse<Organization>> {

    private static final Set<String> VALID_QUERY_PARAMETERS = Set.of(QUERY, PAGE, NUMBER_OF_RESULTS, DEPTH, VERSION,
                                                                     SORT);
    private final transient ClientProvider<CristinQueryApiClient<Map<String, String>, Organization>> clientProvider;

    @JacocoGenerated
    @SuppressWarnings("unused")
    public QueryCristinOrganizationHandler() {
        this(new DefaultOrgQueryClientProvider(), new Environment());
    }

    public QueryCristinOrganizationHandler(
        ClientProvider<CristinQueryApiClient<Map<String, String>, Organization>> clientProvider,
        Environment environment
    ) {
        super(Void.class, environment);
        this.clientProvider = clientProvider;
    }

    @Override
    protected SearchResponse<Organization> processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        validateQueryParameterKeys(requestInfo);
        var requestQueryParams = extractQueryParameters(requestInfo);
        var apiVersion = getApiVersion(requestInfo);

        return clientProvider.getClient(apiVersion).executeQuery(requestQueryParams);
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

    private ConcurrentHashMap<String, String> extractQueryParameters(RequestInfo requestInfo)
        throws BadRequestException {

        var requestQueryParams = new ConcurrentHashMap<String, String>();

        requestQueryParams.put(QUERY, getValidQuery(requestInfo));
        requestQueryParams.put(DEPTH, getValidDepth(requestInfo));
        requestQueryParams.put(PAGE, getValidPage(requestInfo));
        requestQueryParams.put(NUMBER_OF_RESULTS, getValidNumberOfResults(requestInfo));
        getSort(requestInfo).ifPresent(sort -> requestQueryParams.put(SORT, sort));

        return requestQueryParams;
    }

    private Optional<String> getSort(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(SORT)
                   .map(UriUtils::escapeWhiteSpace);
    }

    private String getApiVersion(RequestInfo requestInfo) {
        return extractVersionFromRequestInfo(requestInfo, ACCEPT_HEADER_KEY_NAME);
    }

}
