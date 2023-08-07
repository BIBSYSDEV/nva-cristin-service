package no.unit.nva.cristin.keyword.query;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Collections;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class QueryKeywordsHandlerTest {

    public static final String INVALID_PARAM = "invalid";
    public static final int FIVE_HITS = 5;
    public static final String CRISTIN_KEYWORDS_RESPONSE_JSON = "cristinQueryKeywordsResponse.json";
    public static final String EXPECTED_CRISTIN_URI_WITH_PARAMS =
        "https://api.cristin-test.uio.no/v2/keywords?per_page=5&name=John+Smith&page=1";
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
    @Disabled
    void shouldReturnListOfKeywordsAsDefaultWithoutSpecifyingAnyQueryParams() throws IOException {
        Map<String, String> queryParams = Collections.emptyMap();
        var response = sendQuery(queryParams);
        var responseBody = response.getBodyObject(SearchResponse.class);

        assertThat(response.getStatusCode(), equalTo(HTTP_OK));
        assertThat(responseBody.getHits().size(), equalTo(FIVE_HITS));

        var oneHit = (TypedLabel) responseBody.getHits().get(0);

        assertThat(oneHit, isNotNull());
    }

    @Test
    @Disabled
    void shouldAllowQueryWithValidParametersAndAddThemToUpstreamUri() throws Exception {
        //apiClient = spy(apiClient);
        //handler = new QueryKeywordsHandler(apiClient, environment);
        sendQuery(Collections.emptyMap());

        verify(apiClient).fetchQueryResults(UriWrapper.fromUri(EXPECTED_CRISTIN_URI_WITH_PARAMS).getUri());
    }

    @Test
    @Disabled
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
}
