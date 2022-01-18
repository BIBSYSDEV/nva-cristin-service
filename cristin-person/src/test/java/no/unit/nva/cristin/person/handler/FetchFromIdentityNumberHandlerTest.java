package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.net.HttpHeaders;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.client.CristinPersonApiClientStub;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_PERSON_LOOKUP;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_SERVER_ERROR;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.person.handler.FetchFromIdentityNumberHandler.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.person.handler.FetchFromIdentityNumberHandler.NIN_TYPE;
import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_USERS;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FetchFromIdentityNumberHandlerTest {

    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();
    public static final Map<String, String> INVALID_PAYLOAD = Map.of("SomeKey", "SomeValue");
    private static final String NVA_API_GET_PERSON_RESPONSE_JSON =
        "nvaApiGetPersonResponse.json";
    private static final String DEFAULT_IDENTITY_NUMBER = "07117631634";
    private static final String VALID_CRISTIN_NATIONAL_ID_URI = "https://api.cristin-test.uio"
        + ".no/v2/persons?national_id=07117631634&lang=en,nb,nn";
    private static final String URI_FIRST_HIT_FROM_CRISTIN = "https://api.cristin-test.uio"
        + ".no/v2/persons/359084?lang=en,nb,nn";
    public static final Map<String, String> SOME_QUERY_PARAM = Map.of("SomeQueryParam", "SomeValue");
    public static final String WRONG_TYPE = "WRONGTYPE";
    public static final String WRONG_VALUE = "wrongValue";
    public static final String EMPTY_ARRAY = "[]";

    private final Environment environment = new Environment();
    private CristinPersonApiClient apiClient;
    private Context context;
    private ByteArrayOutputStream output;
    private FetchFromIdentityNumberHandler handler;

    @BeforeEach
    void setUp() {
        apiClient = new CristinPersonApiClientStub();

        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new FetchFromIdentityNumberHandler(apiClient, environment);
    }

    @Test
    void shouldReturnPersonWhenMockedHttpResponseContainsCristinPerson() throws Exception {
        Person actual = sendQuery(defaultBody(), EMPTY_MAP).getBodyObject(Person.class);
        String expectedString = IoUtils.stringFromResources(Path.of(NVA_API_GET_PERSON_RESPONSE_JSON));
        Person expected = OBJECT_MAPPER.readValue(expectedString, Person.class);

        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldProduceCorrectCristinUriFromNationalIdentifier() throws IOException, ApiGatewayException {
        apiClient = spy(apiClient);
        handler = new FetchFromIdentityNumberHandler(apiClient, environment);
        sendQuery(defaultBody(), EMPTY_MAP);

        verify(apiClient).fetchQueryResults(new UriWrapper(VALID_CRISTIN_NATIONAL_ID_URI).getUri());
        verify(apiClient).fetchGetResult(new UriWrapper(URI_FIRST_HIT_FROM_CRISTIN).getUri());
    }

    @Test
    void shouldReturnServerErrorWhenBackendAuthenticationNotSentToUpstreamOrNotValid() throws Exception {
        HttpClient clientMock = mock(HttpClient.class);
        when(clientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_STRING, 401));
        CristinPersonApiClient apiClient = new CristinPersonApiClient(clientMock);
        handler = new FetchFromIdentityNumberHandler(apiClient, environment);
        GatewayResponse<Person> gatewayResponse = sendQuery(defaultBody(), EMPTY_MAP);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_SERVER_ERROR));
    }

    @Test
    void shouldReturnBadRequestWhenPayloadNotMatchingContract() throws Exception {
        GatewayResponse<Person> gatewayResponse = sendInvalidQuery();

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_PAYLOAD));
    }

    @Test
    void shouldReturnBadRequestWhenSupplyingAnyQueryParam() throws Exception {
        GatewayResponse<Person> gatewayResponse = sendQuery(defaultBody(), SOME_QUERY_PARAM);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_PERSON_LOOKUP));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidTypeOrValueSpecified() throws Exception {
        TypedValue invalidType = new TypedValue(WRONG_TYPE, DEFAULT_IDENTITY_NUMBER);
        GatewayResponse<Person> gatewayResponse = sendQuery(invalidType, EMPTY_MAP);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_PAYLOAD));

        TypedValue invalidValue = new TypedValue(NIN_TYPE, WRONG_VALUE);
        gatewayResponse = sendQuery(invalidValue, EMPTY_MAP);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_PAYLOAD));
    }

    @Test
    void shouldReturnNotFoundWhenNoMatch() throws ApiGatewayException, IOException {
        apiClient = spy(apiClient);
        doReturn(new HttpResponseFaker(EMPTY_ARRAY, 200)).when(apiClient).fetchQueryResults(any(URI.class));
        handler = new FetchFromIdentityNumberHandler(apiClient, environment);
        GatewayResponse<Person> gatewayResponse = sendQuery(defaultBody(), EMPTY_MAP);

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, gatewayResponse.getStatusCode());
    }

    private GatewayResponse<Person> sendQuery(TypedValue body, Map<String, String> queryParams)
        throws IOException {

        InputStream input = requestWithParams(body, queryParams);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream requestWithParams(TypedValue body, Map<String, String> queryParams)
        throws JsonProcessingException {

        return new HandlerRequestBuilder<TypedValue>(OBJECT_MAPPER)
            .withAccessRight(EDIT_OWN_INSTITUTION_USERS)
            .withBody(body)
            .withQueryParameters(queryParams)
            .build();
    }

    private TypedValue defaultBody() {
        return new TypedValue(NIN_TYPE, DEFAULT_IDENTITY_NUMBER);
    }

    private GatewayResponse<Person> sendInvalidQuery() throws IOException {
        InputStream input = requestWithInvalidPayload();
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output);
    }

    private InputStream requestWithInvalidPayload() throws JsonProcessingException {
        return new HandlerRequestBuilder<Map<String, String>>(OBJECT_MAPPER)
            .withAccessRight(EDIT_OWN_INSTITUTION_USERS)
            .withBody(INVALID_PAYLOAD)
            .build();
    }
}
