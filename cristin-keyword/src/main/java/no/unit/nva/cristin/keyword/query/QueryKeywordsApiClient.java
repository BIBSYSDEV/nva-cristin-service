package no.unit.nva.cristin.keyword.query;

import static java.util.Arrays.asList;
import static no.unit.nva.client.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.cristin.keyword.KeywordConstants.CRISTIN_KEYWORDS_PATH;
import static no.unit.nva.cristin.keyword.KeywordConstants.KEYWORD_ID_URI;
import static no.unit.nva.utils.UriUtils.createCristinQueryUri;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.common.client.CristinQueryApiClient;
import no.unit.nva.cristin.keyword.model.nva.Keyword;
import no.unit.nva.cristin.keyword.model.nva.adapter.KeywordFromCristin;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.model.SearchResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.paths.UriWrapper;

public class QueryKeywordsApiClient extends ApiClient
    implements CristinQueryApiClient<Map<String, String>, Keyword> {

    public static final String KEYWORD_CONTEXT_JSON = "https://bibsysdev.github.io/src/keyword-search-context.json";

    public QueryKeywordsApiClient() {
        this(defaultHttpClient());
    }

    public QueryKeywordsApiClient(HttpClient client) {
        super(client);
    }

    @Override
    public SearchResponse<Keyword> executeQuery(Map<String, String> params) throws ApiGatewayException {
        var convertedParams = new QueryParamConverter(params).convert().getResult();
        var queryUri = createCristinQueryUri(convertedParams, CRISTIN_KEYWORDS_PATH);
        var start = System.currentTimeMillis();
        var response = queryUpstream(queryUri);
        var keywords = getKeywords(response);
        var totalProcessingTime = calculateProcessingTime(start, System.currentTimeMillis());

        return new SearchResponse<Keyword>(appendSearchStringToId(params))
                   .withContext(KEYWORD_CONTEXT_JSON)
                   .withHits(keywords)
                   .usingHeadersAndQueryParams(response.headers(), params)
                   .withProcessingTime(totalProcessingTime);
    }

    private HttpResponse<String> queryUpstream(URI uri) throws ApiGatewayException {
        var response = fetchQueryResults(uri);
        checkHttpStatusCode(KEYWORD_ID_URI, response.statusCode(), response.body());

        return response;
    }

    private List<Keyword> getKeywords(HttpResponse<String> response) throws BadGatewayException {
        var cristinKeywords = asList(getDeserializedResponse(response, CristinTypedLabel[].class));

        return cristinKeywords.stream()
                   .map(new KeywordFromCristin(false))
                   .collect(Collectors.toList());
    }

    private URI appendSearchStringToId(Map<String, String> params) {
        return UriWrapper.fromUri(KEYWORD_ID_URI).addQueryParameters(params).getUri();
    }

}
