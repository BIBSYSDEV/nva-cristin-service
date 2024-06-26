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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;
import no.unit.nva.cristin.keyword.model.nva.Keyword;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

class CreateKeywordHandlerTest {

    public static final String EMPTY_JSON = "{}";
    public static final String KEYWORD_TYPE = "1197";
    public static final String EN_KEY = "en";
    public static final String NB_KEY = "nb";
    public static final String EN_VALUE = "Educational management";
    public static final String NB_VALUE = "Utdanningsledelse";
    public static final URI UPSTREAM_URI = URI.create("https://api.cristin-test.uio.no/v2/keywords");
    public static final String NVA_CREATE_KEYWORD_RESPONSE_JSON = "nvaFetchKeywordResponse.json";

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
    void shouldCreateAndReturnKeywordWhenClientSendsValidPayload() throws Exception {
        var responseJson = exampleCristinObjectToJson();
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(responseJson, 201));
        apiClient = new CristinCreateKeywordApiClient(httpClientMock);
        handler = new CreateKeywordHandler(environment, apiClient);
        var gatewayResponse = sendQuery(exampleObject());
        var actual = gatewayResponse.getBodyObject(Keyword.class);
        var expected = readExpectedResponse();

        assertEquals(HTTP_CREATED, gatewayResponse.getStatusCode());
        JSONAssert.assertEquals(expected, actual.toString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void shouldHaveCorrectUriAndBodyWhenSendingToUpstream() throws Exception {
        var responseJson = exampleCristinObjectToJson();
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(responseJson, 201));
        var apiClient = new CristinCreateKeywordApiClient(httpClientMock);
        apiClient = spy(apiClient);
        handler = new CreateKeywordHandler(environment, apiClient);
        sendQuery(exampleObject());

        var uriCaptor = ArgumentCaptor.forClass(URI.class);
        var bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(apiClient).post(uriCaptor.capture(), bodyCaptor.capture());

        var actual = OBJECT_MAPPER.readValue(bodyCaptor.getValue(), CristinTypedLabel.class);

        assertThat(uriCaptor.getValue(), equalTo(UPSTREAM_URI));
        assertThat(actual.getCode(), equalTo(null));
        assertThat(actual.getName().get(EN_KEY), equalTo(EN_VALUE));
        assertThat(actual.getName().get(NB_KEY), equalTo(NB_VALUE));
    }

    @Test
    void shouldThrowForbiddenIfUserNotAuthorized() throws IOException {
        var input = exampleObject();
        var gatewayResponse = sendQueryWithoutAccessRights(input);

        assertEquals(HTTP_FORBIDDEN, gatewayResponse.getStatusCode());
    }

    @ParameterizedTest(name = "Payload throws 400 Bad Request: {0}")
    @MethodSource("badRequestProvider")
    void shouldThrowExceptionWhenInputNotValid(Keyword input) throws IOException, InterruptedException {
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

    private static Keyword exampleObject() {
        return new Keyword.Builder().withLabels(exampleLabel()).build();
    }

    private static Map<String, String> exampleLabel() {
        return Map.of(EN_KEY, EN_VALUE,
                      NB_KEY, NB_VALUE);
    }

    private static Stream<Arguments> badRequestProvider() {
        return Stream.of(Arguments.of(new Keyword.Builder().build()),
                         Arguments.of(new Keyword.Builder().withLabels(Map.of(EN_KEY, EN_VALUE)).build()),
                         Arguments.of(new Keyword.Builder().withLabels(Map.of(NB_KEY, NB_VALUE)).build()));
    }

    private GatewayResponse<Keyword> sendQueryWithoutAccessRights(Keyword body) throws IOException {
        var input = new HandlerRequestBuilder<>(OBJECT_MAPPER).withBody(body)
                        .build();
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Keyword.class);
    }

    private GatewayResponse<Keyword> sendQuery(Keyword body) throws IOException {
        var input = requestWithBody(body);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, Keyword.class);
    }

    private InputStream requestWithBody(Keyword body) throws JsonProcessingException {
        final var customerId = randomUri();
        return new HandlerRequestBuilder<Keyword>(OBJECT_MAPPER)
                   .withBody(body)
                   .withCurrentCustomer(customerId)
                   .withAccessRights(customerId, MANAGE_OWN_RESOURCES)
                   .build();
    }

    private String readExpectedResponse() {
        return IoUtils.stringFromResources(Path.of(NVA_CREATE_KEYWORD_RESPONSE_JSON));
    }

}
