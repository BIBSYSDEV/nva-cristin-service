package no.unit.nva.cristin.keyword.query;

import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.common.client.QueryApiClient;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.model.TypedLabel;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;

public class QueryKeywordsHandler extends CristinQueryHandler<Void, SearchResponse<TypedLabel>> {

    public static final String DEFAULT_NUMBER_OF_RESULTS = "100";
    public static final String DEFAULT_PAGE = "1";

    private final transient QueryApiClient<Map<String, String>, TypedLabel> apiClient;

    @SuppressWarnings("unused")
    public QueryKeywordsHandler() {
        this(new Environment());
    }

    public QueryKeywordsHandler(Environment environment) {
        this(new QueryKeywordsApiClient(), environment);
    }

    public QueryKeywordsHandler(QueryApiClient<Map<String, String>, TypedLabel> apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected SearchResponse<TypedLabel> processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        var queryParams = parseQueryParams(requestInfo);

        return apiClient.executeQuery(queryParams);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<TypedLabel> output) {
        return HTTP_OK;
    }

    private ConcurrentHashMap<String, String> parseQueryParams(RequestInfo requestInfo) throws BadRequestException {
        validateQueryParameterKeys(requestInfo);

        var queryParams = new ConcurrentHashMap<String, String>();
        getValidQueryOpt(requestInfo).ifPresent(query -> queryParams.put(NAME, query));
        getValidPageOpt(requestInfo)
            .ifPresentOrElse(page -> queryParams.put(PAGE, page), () -> queryParams.put(PAGE, DEFAULT_PAGE));
        getValidResultsPerPageOpt(requestInfo)
            .ifPresentOrElse(results -> queryParams.put(NUMBER_OF_RESULTS, results),
                             () -> queryParams.put(NUMBER_OF_RESULTS, DEFAULT_NUMBER_OF_RESULTS));

        return queryParams;
    }

    private Optional<String> getValidQueryOpt(RequestInfo requestInfo) {
        return attempt(() -> super.getValidQuery(requestInfo)).toOptional();
    }

    private Optional<String> getValidPageOpt(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(PAGE).filter(Utils::isPositiveInteger);
    }

    private Optional<String> getValidResultsPerPageOpt(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(NUMBER_OF_RESULTS).filter(Utils::isPositiveInteger);
    }

}
