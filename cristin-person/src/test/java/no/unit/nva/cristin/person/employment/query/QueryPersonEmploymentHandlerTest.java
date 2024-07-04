package no.unit.nva.cristin.person.employment.query;

import static no.unit.nva.cristin.common.ErrorMessages.invalidPathParameterMessage;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static no.unit.nva.cristin.person.employment.query.QueryPersonEmploymentClient.BAD_REQUEST_FROM_UPSTREAM;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.AccessRight.MANAGE_OWN_AFFILIATION;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QueryPersonEmploymentHandlerTest {

    public static final TypeReference<SearchResponse<Employment>> SEARCH_RESPONSE_REF = new TypeReference<>() {};
    private static final String VALID_PERSON_ID = "123456";
    private static final String INVALID_IDENTIFIER = "hello";
    private static final String EMPTY_ARRAY = "[]";
    private static final String CRISTIN_QUERY_RESPONSE_JSON =
        "cristinQueryEmploymentResponse.json";
    private static final String DUMMY_CRISTIN_RESPONSE =
        IoUtils.stringFromResources(Path.of(CRISTIN_QUERY_RESPONSE_JSON));
    private static final String NVA_QUERY_RESPONSE_JSON =
        "nvaApiQueryEmploymentResponse.json";
    private static final String EXPECTED_NVA_RESPONSE =
        IoUtils.stringFromResources(Path.of(NVA_QUERY_RESPONSE_JSON));
    private static final String ONE_EMPTY_RESULT = "[{}]";

    private final HttpClient clientMock = mock(HttpClient.class);
    private final Environment environment = new Environment();
    private QueryPersonEmploymentClient apiClient;
    private Context context;
    private ByteArrayOutputStream output;
    private QueryPersonEmploymentHandler handler;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(clientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(DUMMY_CRISTIN_RESPONSE, 200));
        apiClient = new QueryPersonEmploymentClient(clientMock);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new QueryPersonEmploymentHandler(apiClient, environment);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenClientIsNotAuthenticated() throws IOException {
        var gatewayResponse = queryWithoutRequiredAccessRights();

        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldReturnStatusOkWithDataInResponseMatchingUpstreamWhenSuccessfulQuery() throws IOException {
        var expectedEmployments = extractHitsFromSearchResponse(readExpectedResponse());
        var gatewayResponse = sendQuery(Map.of(PERSON_ID, VALID_PERSON_ID));
        var actualEmployments = OBJECT_MAPPER.readValue(gatewayResponse.getBody(), SEARCH_RESPONSE_REF);

        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertEquals(actualEmployments.getHits().size(), 2);
        assertThat(actualEmployments.getHits().containsAll(expectedEmployments), equalTo(true));
    }

    @Test
    void shouldReturnBadRequestWhenCallingHandlerWithInvalidIdentifier() throws IOException {
        var gatewayResponse = sendQuery(Map.of(PERSON_ID, INVALID_IDENTIFIER));

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(invalidPathParameterMessage(PERSON_ID)));
    }

    @Test
    void shouldReturnEmptySearchResponseWhenUpstreamReturnsZeroHits() throws IOException, InterruptedException {
        when(clientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_ARRAY, 200));
        apiClient = new QueryPersonEmploymentClient(clientMock);
        handler = new QueryPersonEmploymentHandler(apiClient, environment);

        var gatewayResponse = this.<Employment>sendQuery(Map.of(PERSON_ID, VALID_PERSON_ID));
        var response = OBJECT_MAPPER.readValue(gatewayResponse.getBody(), SEARCH_RESPONSE_REF);

        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(response.getHits().size(), equalTo(0));
    }

    @Test
    void shouldReturnBadRequestWhenUpstreamReturnsBadRequestIndicatingIdentifierNotFound()
        throws IOException, InterruptedException {

        when(clientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_ARRAY, 400));
        apiClient = new QueryPersonEmploymentClient(clientMock);
        handler = new QueryPersonEmploymentHandler(apiClient, environment);

        var gatewayResponse = sendQuery(Map.of(PERSON_ID, VALID_PERSON_ID));

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(BAD_REQUEST_FROM_UPSTREAM));
    }

    @Test
    void shouldNotThrowNullExceptionWhenReceivingEmptyData() throws IOException, InterruptedException {
        when(clientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(ONE_EMPTY_RESULT, 200));
        apiClient = new QueryPersonEmploymentClient(clientMock);
        handler = new QueryPersonEmploymentHandler(apiClient, environment);
        var gatewayResponse = sendQuery(Map.of(PERSON_ID, VALID_PERSON_ID));
        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
    }

    private List<Employment> extractHitsFromSearchResponse(SearchResponse<?> response) {
        return OBJECT_MAPPER.convertValue(response.getHits(), new TypeReference<>() { });
    }

    private SearchResponse<?> readExpectedResponse() throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(EXPECTED_NVA_RESPONSE, SearchResponse.class);
    }

    private <T> GatewayResponse<SearchResponse<T>> queryWithoutRequiredAccessRights() throws IOException {
        try (var input = getQueryNoAccess()) {
            handler.handleRequest(input, output, context);
        }
        return OBJECT_MAPPER.readValue(output.toString(StandardCharsets.UTF_8), getTypeRef());
    }

    private <T> GatewayResponse<SearchResponse<T>> sendQuery(Map<String, String> pathParam) throws IOException {

        try (var input = requestWithParams(pathParam)) {
            handler.handleRequest(input, output, context);
        }
        return OBJECT_MAPPER.readValue(output.toString(StandardCharsets.UTF_8), getTypeRef());
    }

    private <T> TypeReference<GatewayResponse<SearchResponse<T>>> getTypeRef() {
        return new TypeReference<>() {
        };
    }

    private InputStream requestWithParams(Map<String, String> pathParams) throws JsonProcessingException {
        var customerId = randomUri();
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withCurrentCustomer(customerId)
            .withAccessRights(customerId, MANAGE_OWN_AFFILIATION)
            .withPathParameters(pathParams)
            .build();
    }

    private InputStream getQueryNoAccess() throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                   .withPathParameters(Map.of(PERSON_ID, VALID_PERSON_ID))
                   .build();
    }
}
