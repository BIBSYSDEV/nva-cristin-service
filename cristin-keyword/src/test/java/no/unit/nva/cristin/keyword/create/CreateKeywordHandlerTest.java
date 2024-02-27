package no.unit.nva.cristin.keyword.create;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static nva.commons.apigateway.AccessRight.MANAGE_OWN_RESOURCES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.util.Map;
import java.util.stream.Stream;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CreateKeywordHandlerTest {

    public static final String EMPTY_JSON = "{}";
    public static final String KEYWORD_TYPE = "HELSE";
    public static final String EN_KEY = "en";
    public static final String NB_KEY = "nb";
    public static final String EN_VALUE = "Something about health";
    public static final String NB_VALUE = "Noe om helse";

    private final HttpClient httpClientMock = mock(HttpClient.class);
    private final Environment environment = new Environment();
    private CreateKeywordApiClient apiClient;
    private Context context;
    private ByteArrayOutputStream output;
    private CreateKeywordHandler handler;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 201));
        apiClient = new CristinCreateKeywordApiClient(httpClientMock);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new CreateKeywordHandler(environment, apiClient);
    }

    @Test
    void shouldCreateAndReturnKeywordWhenClientSendsValidPayload() throws IOException, InterruptedException {
        var responseJson = exampleCristinObjectToJson();
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(responseJson, 201));
        apiClient = new CristinCreateKeywordApiClient(httpClientMock);
        handler = new CreateKeywordHandler(environment, apiClient);
        var gatewayResponse = sendQuery(exampleObject());
        var actual = gatewayResponse.getBodyObject(TypedLabel.class);

        assertEquals(HTTP_CREATED, gatewayResponse.getStatusCode());
        assertThat(actual.getType(), equalTo(exampleCristinObject().getCode()));
        assertThat(actual.getLabel(), equalTo(exampleCristinObject().getName()));
    }

    @Test
    void shouldThrowForbiddenIfUserNotAuthorized() throws IOException {
        var input = exampleObject();
        var gatewayResponse = sendQueryWithoutAccessRights(input);

        assertEquals(HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
    }

    @ParameterizedTest(name = "Payload throws 400 Bad Request: {0}")
    @MethodSource("badRequestProvider")
    void shouldThrowExceptionWhenInputNotValid(TypedLabel input) throws IOException, InterruptedException {
        var responseJson = exampleCristinObjectToJson();
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(responseJson, 201));
        apiClient = new CristinCreateKeywordApiClient(httpClientMock);
        handler = new CreateKeywordHandler(environment, apiClient);
        var gatewayResponse = sendQuery(input);

        assertEquals(HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getBody(), containsString(CristinCreateKeywordValidator.ERROR_MESSAGE));
    }

    private static String exampleCristinObjectToJson() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(exampleCristinObject());
    }

    private static CristinTypedLabel exampleCristinObject() {
        return new CristinTypedLabel(KEYWORD_TYPE, exampleLabel());
    }

    private static TypedLabel exampleObject() {
        return new TypedLabel(KEYWORD_TYPE, exampleLabel());
    }

    private static Map<String, String> exampleLabel() {
        return Map.of(EN_KEY, EN_VALUE,
                      NB_KEY, NB_VALUE);
    }

    private static Stream<Arguments> badRequestProvider() {
        return Stream.of(Arguments.of(new TypedLabel(null, null)),
                         Arguments.of(new TypedLabel(KEYWORD_TYPE, null)),
                         Arguments.of(new TypedLabel(KEYWORD_TYPE, Map.of(EN_KEY, EN_VALUE))),
                         Arguments.of(new TypedLabel(KEYWORD_TYPE, Map.of(NB_KEY, NB_VALUE))));
    }

    private GatewayResponse<TypedLabel> sendQueryWithoutAccessRights(TypedLabel body) throws IOException {
        var input = new HandlerRequestBuilder<TypedLabel>(OBJECT_MAPPER).withBody(body)
                        .build();
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, TypedLabel.class);
    }

    private GatewayResponse<TypedLabel> sendQuery(TypedLabel body) throws IOException {
        var input = requestWithBody(body);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, TypedLabel.class);
    }

    private InputStream requestWithBody(TypedLabel body) throws JsonProcessingException {
        final var customerId = randomUri();
        return new HandlerRequestBuilder<TypedLabel>(OBJECT_MAPPER)
                   .withBody(body)
                   .withCurrentCustomer(customerId)
                   .withAccessRights(customerId, MANAGE_OWN_RESOURCES)
                   .build();
    }

}
