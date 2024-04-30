package no.unit.nva.cristin.person.fetch;

import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.person.fetch.FetchFromIdentityNumberHandler.NIN_TYPE;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.utils.AccessUtils.ACCESS_TOKEN_CLAIMS_FIELD;
import static no.unit.nva.utils.AccessUtils.ACCESS_TOKEN_CLAIMS_SCOPE_FIELD;
import static no.unit.nva.utils.AccessUtils.AUTHORIZER_FIELD;
import static nva.commons.apigateway.AccessRight.MANAGE_CUSTOMERS;
import static nva.commons.apigateway.AccessRight.MANAGE_OWN_AFFILIATION;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.client.CristinPersonApiClientStub;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.exception.UnauthorizedException;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FetchFromIdentityNumberHandlerTest {

    public static final Map<String, String> INVALID_PAYLOAD = Map.of("SomeKey", "SomeValue");
    public static final Map<String, String> SOME_QUERY_PARAM = Map.of("SomeQueryParam", "SomeValue");
    public static final String WRONG_TYPE = "WRONGTYPE";
    public static final String WRONG_VALUE = "wrongValue";
    public static final String EMPTY_ARRAY = "[]";
    public static final String ACCESS_TOKEN_BACKEND_SCOPE = "https://api.nva.unit.no/scopes/backend";
    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();
    private static final String NVA_API_GET_PERSON_RESPONSE_JSON =
        "nvaApiGetPersonResponse.json";
    private static final String DEFAULT_IDENTITY_NUMBER = "07117631634";
    private static final String VALID_CRISTIN_NATIONAL_ID_URI =
        "https://api.cristin-test.uio.no/v2/persons?national_id=07117631634";
    private static final String URI_FIRST_HIT_FROM_CRISTIN =
        "https://api.cristin-test.uio.no/v2/persons/359084";
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
        expected = expected.copy().withEmployments(Collections.emptySet()).build();

        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldProduceCristinUriWithCristinIdFromNationalIdentifier() throws IOException, ApiGatewayException {
        apiClient = spy(apiClient);
        handler = new FetchFromIdentityNumberHandler(apiClient, environment);
        sendQuery(defaultBody(), EMPTY_MAP);

        verify(apiClient).fetchQueryResults(UriWrapper.fromUri(VALID_CRISTIN_NATIONAL_ID_URI).getUri());
        verify(apiClient).fetchGetResult(UriWrapper.fromUri(URI_FIRST_HIT_FROM_CRISTIN).getUri());
    }

    @Test
    void shouldReturnServerErrorWhenBackendAuthenticationNotSentToUpstreamOrNotValid() throws Exception {
        HttpClient clientMock = mock(HttpClient.class);
        when(clientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_STRING, 401));
        CristinPersonApiClient apiClient = new CristinPersonApiClient(clientMock);
        handler = new FetchFromIdentityNumberHandler(apiClient, environment);
        GatewayResponse<Person> gatewayResponse = sendQuery(defaultBody(), EMPTY_MAP);

        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(UnauthorizedException.DEFAULT_MESSAGE));
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
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidTypeSpecified() throws Exception {
        TypedValue invalidType = new TypedValue(WRONG_TYPE, DEFAULT_IDENTITY_NUMBER);
        GatewayResponse<Person> gatewayResponse = sendQuery(invalidType, EMPTY_MAP);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        assertThat(gatewayResponse.getBody(), containsString(ERROR_MESSAGE_INVALID_PAYLOAD));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidValueSpecified() throws Exception {
        TypedValue invalidValue = new TypedValue(NIN_TYPE, WRONG_VALUE);
        GatewayResponse<Person> gatewayResponse = sendQuery(invalidValue, EMPTY_MAP);

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

    @Test
    void shouldReturnPersonWhenClientIsAuthenticatedWithAccessTokenContaininingTheBackendScope()
        throws IOException {
        var request = requestWithBackendScope();
        handler.handleRequest(request, output, context);
        GatewayResponse<Person> response = GatewayResponse.fromOutputStream(output, Person.class);
        assertThat(response.getStatusCode(), is(equalTo(HTTP_OK)));
        Person person = response.getBodyObject(Person.class);
        assertThat(person, is(not(nullValue())));
    }

    @Test
    void shouldThrowForbiddenWhenUnauthorizedAccessRight() throws IOException {
        var gatewayResponse = sendQueryWithUnauthorizedAccessRight();

        assertThat(gatewayResponse.getStatusCode(), equalTo(HttpURLConnection.HTTP_FORBIDDEN));
    }

    @ParameterizedTest(name = "Fetch returns 200 OK when having access right: {0}")
    @MethodSource("accessRightProvider")
    void shouldReturnOkWhenHavingRequiredAccessRights(AccessRight accessRight) throws Exception {
        var actual = sendQueryWithAccessRight(defaultBody(), accessRight);

        assertThat(actual.getStatusCode(), equalTo(HTTP_OK));
    }

    private InputStream requestWithBackendScope() throws JsonProcessingException {
        ObjectNode requestContext = OBJECT_MAPPER.createObjectNode();
        ObjectNode authorizerNode = OBJECT_MAPPER.createObjectNode();
        ObjectNode claimsFromAccessToken = OBJECT_MAPPER.createObjectNode();
        claimsFromAccessToken.put(ACCESS_TOKEN_CLAIMS_SCOPE_FIELD, ACCESS_TOKEN_BACKEND_SCOPE);
        authorizerNode.set(ACCESS_TOKEN_CLAIMS_FIELD, claimsFromAccessToken);
        requestContext.set(AUTHORIZER_FIELD, authorizerNode);
        return new HandlerRequestBuilder<TypedValue>(OBJECT_MAPPER)
            .withBody(defaultBody())
            .withRequestContext(requestContext)
            .build();
    }

    private GatewayResponse<Person> sendQueryWithUnauthorizedAccessRight() throws IOException {
        try (var input = requestWithParams(defaultBody(), EMPTY_MAP, AccessRight.MANAGE_IMPORT)) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, Person.class);
    }

    private GatewayResponse<Person> sendQuery(TypedValue body, Map<String, String> queryParams)
        throws IOException {

        try (var input = requestWithParams(body, queryParams, MANAGE_OWN_AFFILIATION)) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, Person.class);
    }

    private InputStream requestWithParams(TypedValue body, Map<String, String> queryParams, AccessRight accessRight)
        throws JsonProcessingException {
        var customerId = randomUri();
        return new HandlerRequestBuilder<TypedValue>(OBJECT_MAPPER)
            .withCurrentCustomer(customerId)
            .withAccessRights(customerId, accessRight)
            .withBody(body)
            .withQueryParameters(queryParams)
            .build();
    }

    private TypedValue defaultBody() {
        return new TypedValue(NIN_TYPE, DEFAULT_IDENTITY_NUMBER);
    }

    private GatewayResponse<Person> sendInvalidQuery() throws IOException {
        try (var input = requestWithInvalidPayload()) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, Person.class);
    }

    private InputStream requestWithInvalidPayload() throws JsonProcessingException {
        var customerId = randomUri();
        return new HandlerRequestBuilder<Map<String, String>>(OBJECT_MAPPER)
             .withCurrentCustomer(customerId)
            .withAccessRights(customerId,MANAGE_OWN_AFFILIATION)
            .withBody(INVALID_PAYLOAD)
            .build();
    }

    private GatewayResponse<Person> sendQueryWithAccessRight(TypedValue body,
                                                             AccessRight accessRight)
        throws IOException {

        try (var input = requestWithParams(body, EMPTY_MAP, accessRight)) {
            handler.handleRequest(input, output, context);
        }
        return GatewayResponse.fromOutputStream(output, Person.class);
    }

    private static Stream<Arguments> accessRightProvider() {
        return Stream.of(Arguments.of(MANAGE_OWN_AFFILIATION),
                         Arguments.of(MANAGE_CUSTOMERS));
    }

}
