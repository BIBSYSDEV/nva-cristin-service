package no.unit.nva.cristin.projects.category;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
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
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.List;
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

class ListCategoriesHandlerTest {

    public static final int NINE_HITS = 9;
    public static final String CRISTIN_CATEGORIES_RESPONSE_JSON = "cristinProjectCategoriesResponse.json";
    public static final String EXPECTED_CRISTIN_URI =
        "https://api.cristin-test.uio.no/v2/projects/categories";
    public static final String NVA_QUERY_CATEGORIES_RESPONSE_JSON = "nvaProjectCategoriesResponse.json";

    private CategoryApiClient apiClient;
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private ListCategoriesHandler handler;

    @BeforeEach
    void setUp() throws ApiGatewayException {
        var httpClient = mock(HttpClient.class);
        apiClient = new CategoryApiClient(httpClient);
        apiClient = spy(apiClient);
        var fakeQueryResponse = IoUtils.stringFromResources(Path.of(CRISTIN_CATEGORIES_RESPONSE_JSON));
        doReturn(new HttpResponseFaker(fakeQueryResponse)).when(apiClient).fetchQueryResults(any());
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new ListCategoriesHandler(apiClient, environment);
    }

    @Test
    void shouldReturnListOfCategoriesAsDefaultWithoutSpecifyingAnyQueryParams() throws IOException {
        var response = sendQuery();
        var responseBody = response.getBodyObject(SearchResponse.class);

        assertThat(response.getStatusCode(), equalTo(HTTP_OK));
        assertThat(responseBody.getHits().size(), equalTo(NINE_HITS));

        var hits = extractHitsFromSearchResponse(responseBody);
        var expectedHits = readExpectedHitsFromResources();

        assertThat(hits.containsAll(expectedHits), equalTo(true));
    }

    @Test
    void shouldCallCorrectUpstreamUri() throws Exception {
        sendQuery();

        verify(apiClient).fetchQueryResults(UriWrapper.fromUri(EXPECTED_CRISTIN_URI).getUri());
    }

    @SuppressWarnings("rawtypes")
    private GatewayResponse<SearchResponse> sendQuery() throws IOException {
        var input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                        .withBody(null)
                        .build();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output, SearchResponse.class);
    }

    @SuppressWarnings("rawtypes")
    private List<TypedLabel> extractHitsFromSearchResponse(SearchResponse response) {
        return OBJECT_MAPPER.convertValue(response.getHits(), new TypeReference<>() {});
    }

    private List<TypedLabel> readExpectedHitsFromResources() throws JsonProcessingException {
        var resource = IoUtils.stringFromResources(Path.of(NVA_QUERY_CATEGORIES_RESPONSE_JSON));
        return asList(OBJECT_MAPPER.readValue(resource, TypedLabel[].class));
    }

}