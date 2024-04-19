package no.unit.nva.cristin.keyword.fetch;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Map;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

class FetchKeywordHandlerTest {

    public static final String CRISTIN_FETCH_KEYWORD_RESPONSE_JSON = "cristinFetchKeywordResponse.json";
    public static final URI EXPECTED_CRISTIN_URI =
        URI.create("https://api.cristin-test.uio.no/v2/keywords/1197");
    public static final String NVA_FETCH_KEYWORD_RESPONSE_JSON = "nvaFetchKeywordResponse.json";
    public static final String SAMPLE_IDENTIFIER = "1197";
    public static final String SOME_STRING = "someString";

    private FetchCristinKeywordApiClient apiClient;
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private FetchKeywordHandler handler;

    @BeforeEach
    void setUp() throws ApiGatewayException {
        var httpClient = mock(HttpClient.class);
        apiClient = new FetchCristinKeywordApiClient(httpClient);
        apiClient = spy(apiClient);
        var fakeResponse = IoUtils.stringFromResources(Path.of(CRISTIN_FETCH_KEYWORD_RESPONSE_JSON));
        doReturn(new HttpResponseFaker(fakeResponse)).when(apiClient).fetchGetResult(any());
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new FetchKeywordHandler(apiClient, environment);
    }

    @Test
    void shouldReturnKeywordWhenCallingHandlerWithValidIdentifier() throws Exception {
        var response = sendQuery(SAMPLE_IDENTIFIER);
        var actualBody = response.getBody();
        var expectedBody = readExpectedResponse();

        assertThat(response.getStatusCode(), equalTo(HTTP_OK));
        JSONAssert.assertEquals(expectedBody, actualBody, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void shouldCallCorrectUpstreamUriWhenExecutingRequestWithIdentifier() throws Exception {
        sendQuery(SAMPLE_IDENTIFIER);

        verify(apiClient).fetchGetResult(EXPECTED_CRISTIN_URI);
    }

    @Test
    void shouldThrowBadRequestWhenIdentifierNotANumber() throws Exception {
        var response = sendQuery(SOME_STRING);

        assertThat(response.getStatusCode(), equalTo(HTTP_BAD_REQUEST));
    }

    private GatewayResponse<TypedLabel> sendQuery(String identifier) throws IOException {
        var input = requestWithIdentifier(identifier);
        handler.handleRequest(input, output, context);
        return GatewayResponse.fromOutputStream(output, TypedLabel.class);
    }

    private InputStream requestWithIdentifier(String identifier) throws JsonProcessingException {
        return new HandlerRequestBuilder<Void>(OBJECT_MAPPER)
                   .withBody(null)
                   .withPathParameters(Map.of(ID, identifier))
                   .build();
    }

    private String readExpectedResponse() {
        return IoUtils.stringFromResources(Path.of(NVA_FETCH_KEYWORD_RESPONSE_JSON));
    }

}
