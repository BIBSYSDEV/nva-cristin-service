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
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.model.TypedLabel;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.paths.UriWrapper;

public class QueryKeywordsApiClient extends ApiClient
    implements CristinQueryApiClient<Map<String, String>, TypedLabel> {

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

        return new SearchResponse<TypedLabel>(appendSearchStringToId(params))
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

    private URI appendSearchStringToId(Map<String, String> params) {
        return UriWrapper.fromUri(KEYWORD_ID_URI).addQueryParameters(params).getUri();
    }

}
