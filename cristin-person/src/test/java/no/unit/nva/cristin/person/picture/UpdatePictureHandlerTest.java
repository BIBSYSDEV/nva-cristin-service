package no.unit.nva.cristin.person.picture;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static no.unit.nva.cristin.common.client.PatchApiClient.EMPTY_JSON;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.utils.UriUtils.PERSON;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static nva.commons.apigateway.MediaTypes.APPLICATION_PROBLEM_JSON;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Base64;
import java.util.Map;
import no.unit.nva.cristin.person.model.nva.Binary;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UpdatePictureHandlerTest {

    private static final String PERSON_CRISTIN_ID = "123456";
    public static final URI PERSON_IDENTIFIER_URI = getNvaApiId(PERSON_CRISTIN_ID, PERSON);
    private static final Map<String, String> validPath = Map.of(PERSON_ID, PERSON_CRISTIN_ID);
    private static final String PROFILE_PICTURE_JPG = "profilePicture.jpg";

    private final HttpClient httpClientMock = mock(HttpClient.class);
    private final Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private UpdatePictureHandler handler;
    private PictureApiClient apiClient;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        when(httpClientMock.<String>send(any(), any())).thenReturn(new HttpResponseFaker(EMPTY_JSON, 204));
        apiClient = new PictureApiClient(httpClientMock);
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new UpdatePictureHandler(apiClient, environment);
    }

    @Test
    void shouldParseInputAndReturnStatusNoContent() throws Exception {
        try (InputStream picture = IoUtils.inputStreamFromResources(PROFILE_PICTURE_JPG)) {
            var encoded = Base64.getEncoder().encodeToString(picture.readAllBytes());
            var input = new Binary(encoded);
            var response = queryWithBody(input);

            assertThat(response.getStatusCode(), equalTo(HTTP_NO_CONTENT));
        }
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenClientIsNotAuthenticated() throws IOException {
        var gatewayResponse = queryWithoutRequiredAccessRights(new Binary(randomString()));

        assertEquals(HTTP_UNAUTHORIZED, gatewayResponse.getStatusCode());
        assertEquals(APPLICATION_PROBLEM_JSON.toString(), gatewayResponse.getHeaders().get(CONTENT_TYPE));
    }

    private GatewayResponse<Void> queryWithoutRequiredAccessRights(Binary body) throws IOException {
        var input = new HandlerRequestBuilder<Binary>(OBJECT_MAPPER)
                        .withBody(body)
                        .withPathParameters(validPath)
                        .build();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output, Void.class);
    }

    private GatewayResponse<Void> queryWithBody(Binary body) throws IOException {
        var input = new HandlerRequestBuilder<Binary>(OBJECT_MAPPER)
                        .withBody(body)
                        .withPathParameters(validPath)
                        .withPersonCristinId(PERSON_IDENTIFIER_URI)
                        .build();
        handler.handleRequest(input, output, context);

        return GatewayResponse.fromOutputStream(output, Void.class);
    }

}
