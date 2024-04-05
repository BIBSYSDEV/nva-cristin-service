package no.unit.nva.cristin.intermediate.storage;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.core.Environment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.stubbing.Answer;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class UpdateUnitsHandlerTest {
    public static final String TEST_BUCKET = "test-bucket";
    public static final String DELIMITER = "\\A";
    public static final String UNITS_JSON_FILENAME = "units-norway.json";
    private Environment environment;
    private S3Client s3Client;
    private HttpClient httpClient;
    private Context context;
    private ByteArrayOutputStream output;
    private InputStream input;

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        this.environment = mock(Environment.class);
        this.s3Client = mock(S3Client.class);
        this.httpClient = mock(HttpClient.class);
        this.context = mock(Context.class);
        this.output = new ByteArrayOutputStream();
        this.input = new HandlerRequestBuilder<Void>(OBJECT_MAPPER).build();
        when(httpClient.send(any(HttpRequest.class),
                             ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenAnswer(
            (Answer<HttpResponse<String>>) invocationOnMock -> {
                HttpRequest request = invocationOnMock.getArgument(0);
                int lastDigit =
                    Character.getNumericValue(request.uri().toString().charAt(request.uri().toString().length() - 1));
                var result = IoUtils.stringFromResources(Path.of("cristinUnits/units%d.json".formatted(lastDigit)));
                HttpResponse<String> httpResponse = mock(HttpResponse.class);
                when(httpResponse.body()).thenReturn(result);
                return httpResponse;
            });

        when(environment.readEnv("CRISTIN_API_URL")).thenReturn("https://api.unittest.nva.aws.sikt.no");
        when(environment.readEnv("INTERMEDIATE_STORAGE_BUCKET_NAME")).thenReturn(TEST_BUCKET);
        when(environment.readEnv("CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME")).thenReturn("cristinBotFilterBypassHeaderName");
        when(environment.readEnv("CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE")).thenReturn("cristinBotFilterBypassHeaderValue");
    }

    @Test
    void whenHandlerIsTriggeredS3ShouldReceiveFile() throws IOException {
        var handler = new UpdateUnitsHandler(environment, s3Client, httpClient);

        handler.handleRequest(input, output, context);

        var putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        var requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

        verify(s3Client).putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture());

        var capturedPutObjectRequest = putObjectRequestCaptor.getValue();
        var capturedRequestBody = requestBodyCaptor.getValue();
        var contentStreamProvider = capturedRequestBody.contentStreamProvider();
        var fileContent = getFileContent(contentStreamProvider);

        assertEquals(TEST_BUCKET, capturedPutObjectRequest.bucket());
        assertEquals(UNITS_JSON_FILENAME, capturedPutObjectRequest.key());
        assertEquals(1370603, fileContent.length());
        assertTrue(fileContent.contains("https://api.cristin.no/v2/units/184.13.53.0"));
    }

    private static String getFileContent(ContentStreamProvider contentStreamProvider) {
        return new Scanner(contentStreamProvider.newStream(), StandardCharsets.UTF_8).useDelimiter(DELIMITER).next();
    }
}
