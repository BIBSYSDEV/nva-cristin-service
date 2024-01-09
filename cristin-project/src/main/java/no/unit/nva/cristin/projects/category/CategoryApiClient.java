package no.unit.nva.cristin.projects.category;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static no.unit.nva.client.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.utils.UriUtils.createCristinQueryUri;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
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

public class CategoryApiClient extends ApiClient implements CristinQueryApiClient<Void, TypedLabel> {

    public static final String CATEGORY_PATH = "category/project";
    public static final URI CATEGORY_ID_URI = getNvaApiUri(CATEGORY_PATH);
    public static final String CRISTIN_CATEGORIES_PATH = "projects/categories";
    public static final String CATEGORY_CONTEXT_JSON = "https://example.org/category-context.json";

    public CategoryApiClient() {
        this(defaultHttpClient());
    }

    public CategoryApiClient(HttpClient client) {
        super(client);
    }

    @Override
    public SearchResponse<TypedLabel> executeQuery(Void noValue) throws ApiGatewayException {
        Map<String, String> emptyParams = emptyMap();
        var queryUri = createCristinQueryUri(emptyParams, CRISTIN_CATEGORIES_PATH);
        var start = System.currentTimeMillis();
        var response = queryUpstream(queryUri);
        var categories = getCategories(response);
        var totalProcessingTime = calculateProcessingTime(start, System.currentTimeMillis());
        var dummyParams = Map.of(PAGE, "1", NUMBER_OF_RESULTS, "2000");

        return new SearchResponse<TypedLabel>(generateIdUri())
                   .withContext(CATEGORY_CONTEXT_JSON)
                   .withHits(categories)
                   .usingHeadersAndQueryParams(response.headers(), dummyParams)
                   .withProcessingTime(totalProcessingTime);
    }

    private HttpResponse<String> queryUpstream(URI uri) throws ApiGatewayException {
        var response = fetchQueryResults(uri);
        checkHttpStatusCode(CATEGORY_ID_URI, response.statusCode(), response.body());

        return response;
    }

    private List<TypedLabel> getCategories(HttpResponse<String> response) throws BadGatewayException {
        var categories = asList(getDeserializedResponse(response, CristinTypedLabel[].class));

        return categories.stream()
                   .map(category -> new TypedLabel(category.getCode(), category.getName()))
                   .collect(Collectors.toList());
    }

    private URI generateIdUri() {
        return UriWrapper.fromUri(CATEGORY_ID_URI).getUri();
    }

}
