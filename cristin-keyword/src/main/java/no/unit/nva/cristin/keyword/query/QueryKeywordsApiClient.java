package no.unit.nva.cristin.keyword.query;

import static java.util.Arrays.asList;
import static no.unit.nva.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.utils.UriUtils.createCristinQueryUri;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.common.client.QueryApiClient;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.model.TypedLabel;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;

public class QueryKeywordsApiClient extends ApiClient implements QueryApiClient<Map<String, String>, TypedLabel> {

    public static final String KEYWORD_PATH = "keyword";
    public static final URI KEYWORD_ID_URI = getNvaApiUri(KEYWORD_PATH);
    public static final String CRISTIN_KEYWORDS_PATH = "keywords";
    public static final String KEYWORD_CONTEXT_JSON = "https://example.org/keyword-context.json";

    public QueryKeywordsApiClient() {
        this(defaultHttpClient());
    }

    public QueryKeywordsApiClient(HttpClient client) {
        super(client);
    }

    @Override
    public SearchResponse<TypedLabel> executeQuery(Map<String, String> params) throws ApiGatewayException {
        var convertedParams = new QueryParamConverter(params).convert().getResult();
        var queryUri = createCristinQueryUri(convertedParams, CRISTIN_KEYWORDS_PATH);
        var start = System.currentTimeMillis();
        var response = queryUpstream(queryUri);
        var keywords = getKeywords(response);
        var totalProcessingTime = calculateProcessingTime(start, System.currentTimeMillis());

        return new SearchResponse<TypedLabel>(KEYWORD_ID_URI)
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

    private List<TypedLabel> getKeywords(HttpResponse<String> response) throws BadGatewayException {
        var keywords = asList(getDeserializedResponse(response, CristinTypedLabel[].class));

        return keywords.stream()
                   .map(keyword -> new TypedLabel(keyword.getCode(), keyword.getName()))
                   .collect(Collectors.toList());
    }

}
