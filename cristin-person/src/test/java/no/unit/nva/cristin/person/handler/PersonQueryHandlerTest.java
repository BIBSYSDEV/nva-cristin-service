package no.unit.nva.cristin.person.handler;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_SERVER_ERROR;
import static no.unit.nva.cristin.testing.HttpResponseFaker.LINK_EXAMPLE_VALUE;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.client.CristinPersonApiClientStub;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PersonQueryHandlerTest {

    private static final String RANDOM_NAME = "John Smith";
    private static final String NVA_API_QUERY_PERSON_JSON =
        "nvaApiQueryPersonResponse.json";
    private static final String PROBLEM_JSON = APPLICATION_PROBLEM_JSON.toString();
    private static final String ZERO_VALUE = "0";
    private static final String ALLOW_ALL_ORIGIN = "*";
    private static final String EMPTY_LIST_STRING = "[]";

    private CristinPersonApiClient apiClient;
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private PersonQueryHandler handler;

    @BeforeEach
    void setUp() {
        apiClient = new CristinPersonApiClientStub();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new PersonQueryHandler(apiClient, environment);
    }

    @Test
    void shouldReturnResponseWhenCallingEndpointWithNameParameter() throws IOException {
        SearchResponse<Person> actual = sendDefaultQuery().getBodyObject(SearchResponse.class);
        String expectedString = IoUtils.stringFromResources(Path.of(NVA_API_QUERY_PERSON_JSON));
        SearchResponse<Person> expected = OBJECT_MAPPER.readValue(expectedString, SearchResponse.class);

        // Type casting problems when using generic types. Needed to convert. Was somehow converting to LinkedHashMap
        List<Person> expectedPersons = OBJECT_MAPPER.convertValue(expected.getHits(), new TypeReference<>() {});
        List<Person> actualPersons = OBJECT_MAPPER.convertValue(actual.getHits(), new TypeReference<>() {});
        expected.setHits(expectedPersons);
        actual.setHits(actualPersons);

        assertEquals(expected, actual);
    }

    @Test
    void shouldHideInternalExceptionFromClientWhenUnknownErrorOccur() throws IOException, ApiGatewayException {
        apiClient = spy(apiClient);
        doThrow(new RuntimeException()).when(apiClient).getEnrichedPersonsUsingQueryResponse(any());
        handler = new PersonQueryHandler(apiClient, environment);

        GatewayResponse<SearchResponse> gatewayResponse = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_SERVER_ERROR));
    }

    @Test
    void shouldReturnBadGatewayToClientWhenBackendFetchFails() throws IOException {
        apiClient = spy(apiClient);
        HttpResponse<String> response = new HttpResponseFaker(EMPTY_STRING, HttpURLConnection.HTTP_INTERNAL_ERROR);
        doReturn(response).when(apiClient).fetchQueryResults(any());
        handler = new PersonQueryHandler(apiClient, environment);

        GatewayResponse<SearchResponse> gatewayResponse = sendDefaultQuery();

        assertEquals(HttpURLConnection.HTTP_BAD_GATEWAY, gatewayResponse.getStatusCode());
        assertEquals(PROBLEM_JSON, gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_BACKEND_FETCH_FAILED));
    }

    @Test
    void shouldReturnResponseFromQueryInsteadOfEnrichedGetWhenEnrichingFails() throws IOException {
        apiClient = spy(apiClient);
        HttpResponse<String> response =
            new HttpResponseFaker(EMPTY_STRING, HttpURLConnection.HTTP_INTERNAL_ERROR);
        doReturn(CompletableFuture.completedFuture(response)).when(apiClient).fetchGetResultAsync(any());
        handler = new PersonQueryHandler(apiClient, environment);
        SearchResponse searchResponse = sendDefaultQuery().getBodyObject(SearchResponse.class);

        // Query response has size of 2, and will return that even if trying to enrich those 2 returns empty
        assertThat(2, equalTo(searchResponse.getHits().size()));
    }

    @Test
    void shouldHaveCorsHeaderAllOriginOnResponse() throws Exception {
        var gatewayResponse = sendDefaultQuery();
        assertEquals(ALLOW_ALL_ORIGIN, gatewayResponse.getHeaders().get(ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void shouldReturnSearchResponseWithEmptyHitsWhenBackendFetchIsEmpty() throws ApiGatewayException, IOException {
        apiClient = spy(apiClient);
        doReturn(new HttpResponseFaker(EMPTY_LIST_STRING, HttpURLConnection.HTTP_OK,
            generateHeaders(ZERO_VALUE, LINK_EXAMPLE_VALUE))).when(apiClient).queryPersons(any());
        doReturn(Collections.emptyList()).when(apiClient).fetchQueryResultsOneByOne(any());
        handler = new PersonQueryHandler(apiClient, environment);
        SearchResponse<Person> searchResponse = sendDefaultQuery().getBodyObject(SearchResponse.class);

        assertThat(0, equalTo(searchResponse.getHits().size()));
    }

    private GatewayResponse<SearchResponse> sendDefaultQuery() throws IOException {
        InputStream input = requestWithQueryParameters(Map.of(QUERY, RANDOM_NAME));
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream requestWithQueryParameters(Map<String, String> map) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withQueryParameters(map)
            .build();
    }

    private java.net.http.HttpHeaders generateHeaders(String totalCount, String link) {
        return java.net.http.HttpHeaders.of(HttpResponseFaker.headerMap(totalCount, link), HttpResponseFaker.filter());
    }
}
