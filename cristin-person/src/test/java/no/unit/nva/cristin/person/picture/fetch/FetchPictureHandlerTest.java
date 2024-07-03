package no.unit.nva.cristin.person.picture.fetch;

import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.util.Base64;
import java.util.Map;
import no.unit.nva.cristin.person.model.nva.Binary;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FetchPictureHandlerTest {

    private static final String PERSON_CRISTIN_ID = "123456";
    private static final Map<String, String> validPath = Map.of(PERSON_ID, PERSON_CRISTIN_ID);
    private static final String PROFILE_PICTURE_JPG = "profilePicture.jpg";
    public static final int PICTURE_SIZE_IN_BYTES = 14941;

    private final HttpClient httpClientMock = mock(HttpClient.class);
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private FetchPictureHandler handler;
    private FetchPictureApiClient apiClient;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        HttpResponse<byte[]> httpResponseMock = mock(HttpResponse.class);
        when(httpResponseMock.body()).thenReturn(pictureToByteArray());
        when(httpResponseMock.statusCode()).thenReturn(200);
        when(httpClientMock.<byte[]>send(any(), any())).thenReturn(httpResponseMock);
        apiClient = new FetchPictureApiClient(httpClientMock);
        apiClient = spy(apiClient);
        doReturn(randomString()).when(apiClient).readBasicAuthHeader();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new FetchPictureHandler(apiClient, environment);
    }

    @Test
    void shouldReturnPictureFromResponse() throws Exception {
        var response = sendQuery();
        var responseBody = response.getBodyObject(Binary.class);
        var responseAsBytes = Base64.getDecoder().decode(responseBody.base64Data());

        assertThat(response.getStatusCode(), equalTo(HTTP_OK));
        assertThat(responseAsBytes.length, equalTo(PICTURE_SIZE_IN_BYTES));
    }

    @Test
    void shouldReturnBadGatewayWhenRequestFails() throws Exception {
        when(httpClientMock.<byte[]>send(any(), any())). thenThrow(HttpTimeoutException.class);
        apiClient = new FetchPictureApiClient(httpClientMock);
        apiClient = spy(apiClient);
        doReturn(randomString()).when(apiClient).readBasicAuthHeader();
        handler = new FetchPictureHandler(apiClient, environment);

        var response = sendQuery();

        assertThat(response.getStatusCode(), equalTo(HTTP_BAD_GATEWAY));
    }

    private byte[] pictureToByteArray() throws IOException {
        return IoUtils.inputStreamToBytes(IoUtils.inputStreamFromResources(PROFILE_PICTURE_JPG));
    }

    private GatewayResponse<Binary> sendQuery() throws IOException {
        var input = new HandlerRequestBuilder<Binary>(OBJECT_MAPPER)
                        .withPathParameters(validPath)
                        .build();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output, Binary.class);
    }

}
