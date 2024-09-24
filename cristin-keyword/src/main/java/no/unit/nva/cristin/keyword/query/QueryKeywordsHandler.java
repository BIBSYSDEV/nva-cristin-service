package no.unit.nva.cristin.keyword.query;

import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.common.client.CristinQueryApiClient;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.keyword.model.nva.Keyword;
import no.unit.nva.cristin.model.SearchResponse;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;

public class QueryKeywordsHandler extends CristinQueryHandler<Void, SearchResponse<Keyword>> {

    public static final String DEFAULT_NUMBER_OF_RESULTS = "100";
    public static final String DEFAULT_PAGE = "1";

    private final transient CristinQueryApiClient<Map<String, String>, Keyword> apiClient;

    @SuppressWarnings("unused")
    public QueryKeywordsHandler() {
        this(new Environment());
    }

    public QueryKeywordsHandler(Environment environment) {
        this(new QueryKeywordsApiClient(), environment);
    }

    public QueryKeywordsHandler(CristinQueryApiClient<Map<String, String>, Keyword> apiClient,
                                Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected void validateRequest(Void input, RequestInfo requestInfo, Context context) {
        // no-op
    }

    @Override
    protected SearchResponse<Keyword> processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        var queryParams = parseQueryParams(requestInfo);

        return apiClient.executeQuery(queryParams);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<Keyword> output) {
        return HTTP_OK;
    }

    private Map<String, String> parseQueryParams(RequestInfo requestInfo) throws BadRequestException {
        validateQueryParameterKeys(requestInfo);

        var queryParams = new ConcurrentHashMap<String, String>();

        requestInfo.getQueryParameterOpt(QUERY).ifPresent(query -> queryParams.put(QUERY, query));

        getValidPageOpt(requestInfo).ifPresentOrElse(
            page -> queryParams.put(PAGE, page),
            () -> queryParams.put(PAGE, DEFAULT_PAGE)
        );

        getValidResultsPerPageOpt(requestInfo).ifPresentOrElse(
            results -> queryParams.put(NUMBER_OF_RESULTS, results),
            () -> queryParams.put(NUMBER_OF_RESULTS, DEFAULT_NUMBER_OF_RESULTS)
        );

        return queryParams;
    }

    private Optional<String> getValidPageOpt(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(PAGE)
                   .filter(Utils::isPositiveInteger);
    }

    private Optional<String> getValidResultsPerPageOpt(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(NUMBER_OF_RESULTS)
                   .filter(Utils::isPositiveInteger);
    }

}
