package no.unit.nva.cristin.keyword.query;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryKeywordsHandlerTest {

    public static final String INVALID_PARAM = "invalid";
    public static final int FIVE_HITS = 5;
    public static final String CRISTIN_KEYWORDS_RESPONSE_JSON = "cristinQueryKeywordsResponse.json";
    public static final String EXPECTED_CRISTIN_URI_WITH_DEFAULT_PARAMS =
        "https://api.cristin-test.uio.no/v2/keywords?per_page=100&page=1";
    public static final String EXPECTED_CRISTIN_URI_WITH_QUERY_PARAM =
        "https://api.cristin-test.uio.no/v2/keywords?per_page=100&name=Biology&page=1";
    public static final String QUERY_VALUE = "Biology";
    public static final String NVA_QUERY_KEYWORDS_RESPONSE_JSON = "nvaQueryKeywordsResponse.json";

    private QueryKeywordsApiClient apiClient;
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private QueryKeywordsHandler handler;

    @BeforeEach
    void setUp() throws ApiGatewayException {
        var httpClient = mock(HttpClient.class);
        apiClient = new QueryKeywordsApiClient(httpClient);
        apiClient = spy(apiClient);
        var fakeQueryResponse = IoUtils.stringFromResources(Path.of(CRISTIN_KEYWORDS_RESPONSE_JSON));
        doReturn(new HttpResponseFaker(fakeQueryResponse)).when(apiClient).fetchQueryResults(any());
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new QueryKeywordsHandler(apiClient, environment);
    }

    @Test
    void shouldReturnListOfKeywordsAsDefaultWithoutSpecifyingAnyQueryParams() throws IOException {
        Map<String, String> queryParams = Collections.emptyMap();
        var response = sendQuery(queryParams);
        var responseBody = response.getBodyObject(SearchResponse.class);

        assertThat(response.getStatusCode(), equalTo(HTTP_OK));
        assertThat(responseBody.getHits().size(), equalTo(FIVE_HITS));

        var hits = extractHitsFromSearchResponse(responseBody);
        var expectedHits = readExpectedHitsFromResources();

        assertThat(hits.containsAll(expectedHits), equalTo(true));
    }

    @Test
    void shouldAllowQueryWithDefaultParametersAndAddThemToUpstreamUri() throws Exception {
        sendQuery(Collections.emptyMap());

        verify(apiClient).fetchQueryResults(UriWrapper.fromUri(EXPECTED_CRISTIN_URI_WITH_DEFAULT_PARAMS).getUri());
    }

    @Test
    void shouldAllowQueryWithQueryParameterAddedToUpstreamUri() throws Exception {
        sendQuery(Map.of(QUERY, QUERY_VALUE));

        verify(apiClient).fetchQueryResults(UriWrapper.fromUri(EXPECTED_CRISTIN_URI_WITH_QUERY_PARAM).getUri());
    }

    @Test
    void shouldThrowBadRequestOnInvalidQueryParams() throws IOException {
        var queryParams = Map.of(INVALID_PARAM, INVALID_PARAM);
        var response = sendQuery(queryParams);

        assertThat(response.getStatusCode(), equalTo(HTTP_BAD_REQUEST));
    }

    @SuppressWarnings("rawtypes")
    private GatewayResponse<SearchResponse> sendQuery(Map<String, String> queryParams) throws IOException {
        var input = requestWithQueryParameters(queryParams);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, SearchResponse.class);
    }

    private InputStream requestWithQueryParameters(Map<String, String> map) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                   .withBody(null)
                   .withQueryParameters(map)
                   .build();
    }

    @SuppressWarnings("rawtypes")
    private List<TypedLabel> extractHitsFromSearchResponse(SearchResponse response) {
        return OBJECT_MAPPER.convertValue(response.getHits(), new TypeReference<>() {});
    }

    private List<TypedLabel> readExpectedHitsFromResources() throws JsonProcessingException {
        var resource = IoUtils.stringFromResources(Path.of(NVA_QUERY_KEYWORDS_RESPONSE_JSON));
        return asList(OBJECT_MAPPER.readValue(resource, TypedLabel[].class));
    }
}
