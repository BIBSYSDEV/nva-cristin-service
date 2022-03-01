package no.unit.nva.cristin.person.employment.query;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PERSON_ID;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static no.unit.nva.cristin.person.employment.query.QueryPersonEmploymentClient.BAD_REQUEST_FROM_UPSTREAM;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_USERS;
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
import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.util.Map;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QueryPersonEmploymentHandlerTest {

    private static final String VALID_PERSON_ID = "123456";
    private static final String INVALID_IDENTIFIER = "hello";
    private static final String DUMMY_RESPONSE = "[{\"id\": \"123456\"}]";
    private static final String EMPTY_ARRAY = "[]";

    private final HttpClient clientMock = mock(HttpClient.class);
    private final Environment environment = new Environment();
    private QueryPersonEmploymentClient apiClient;
    private Context context;
    private ByteArrayOutputStream output;
    private QueryPersonEmploymentHandler handler;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(clientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(DUMMY_RESPONSE, 200));
        apiClient = new QueryPersonEmploymentClient(clientMock);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new QueryPersonEmploymentHandler(apiClient, environment);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenClientIsNotAuthenticated() throws IOException {
        GatewayResponse<SearchResponse> gatewayResponse = queryWithoutRequiredAccessRights();

        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void shouldReturnStatusOkWhenCallingHandlerWithValidIdentifier() throws IOException {
        GatewayResponse<SearchResponse> gatewayResponse = sendQuery(Map.of(PERSON_ID, VALID_PERSON_ID));

        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenCallingHandlerWithInvalidIdentifier() throws IOException {
        GatewayResponse<SearchResponse> gatewayResponse = sendQuery(Map.of(PERSON_ID, INVALID_IDENTIFIER));

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_PERSON_ID));
    }

    @Test
    void shouldReturnEmptySearchResponseWhenUpstreamReturnsZeroHits() throws IOException, InterruptedException {
        when(clientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_ARRAY, 200));
        apiClient = new QueryPersonEmploymentClient(clientMock);
        handler = new QueryPersonEmploymentHandler(apiClient, environment);

        GatewayResponse<SearchResponse> gatewayResponse = sendQuery(Map.of(PERSON_ID, VALID_PERSON_ID));
        SearchResponse response = gatewayResponse.getBodyObject(SearchResponse.class);

        assertEquals(HttpURLConnection.HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(response.getHits().size(), equalTo(0));
    }

    @Test
    void shouldReturnBadRequestWhenUpstreamReturnsBadRequestIndicatingIdentifierNotFound()
        throws IOException, InterruptedException {

        when(clientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_ARRAY, 400));
        apiClient = new QueryPersonEmploymentClient(clientMock);
        handler = new QueryPersonEmploymentHandler(apiClient, environment);

        GatewayResponse<SearchResponse> gatewayResponse = sendQuery(Map.of(PERSON_ID, VALID_PERSON_ID));

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(BAD_REQUEST_FROM_UPSTREAM));
    }

    private GatewayResponse<SearchResponse> queryWithoutRequiredAccessRights() throws IOException {
        InputStream input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withPathParameters(Map.of(PERSON_ID, VALID_PERSON_ID))
            .build();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output);
    }

    private GatewayResponse<SearchResponse> sendQuery(Map<String, String> pathParam)
        throws IOException {

        InputStream input = requestWithParams(pathParam);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream requestWithParams(Map<String, String> pathParams)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
            .withBody(null)
            .withAccessRight(EDIT_OWN_INSTITUTION_USERS)
            .withPathParameters(pathParams)
            .build();
    }
}
