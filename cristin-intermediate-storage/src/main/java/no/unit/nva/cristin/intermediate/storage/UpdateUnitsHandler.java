package no.unit.nva.cristin.intermediate.storage;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import nva.commons.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class UpdateUnitsHandler implements RequestStreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpdateUnitsHandler.class);
    private static final String BUCKET_NAME_ENV = "INTERMEDIATE_STORAGE_BUCKET_NAME";
    private static final String BUCKET_KEY_FILE_NAME = "units.json";
    private static final String CRISTIN_API_URL_ENV = "CRISTIN_API_URL";
    public static final String CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME_ENV = "CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME";
    public static final String CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE_ENV = "CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE";
    private final Environment environment;
    private final S3Client s3Client;
    private final HttpClient httpClient;

    public UpdateUnitsHandler() {
        this(new Environment(), S3Client.create(), HttpClient.newHttpClient());
    }

    public UpdateUnitsHandler(Environment environment, S3Client s3Client, HttpClient httpClient) {
        this.environment = environment;
        this.s3Client = s3Client;
        this.httpClient = httpClient;
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        logger.info("Loading data...");
        String jsonData = loadData();

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                                             .bucket(environment.readEnv(BUCKET_NAME_ENV))
                                             .key(BUCKET_KEY_FILE_NAME)
                                             .build();

        s3Client.putObject(objectRequest, RequestBody.fromString(jsonData));
        logger.info("Data loaded successfully. Length of data {}", jsonData.length());
    }

    private String loadData() {
        var cristinUntisUtil = new CristinUnitsUtil(httpClient, URI.create(environment.readEnv(CRISTIN_API_URL_ENV)),
                                                    environment,
                                                    UpdateUnitsHandler.class,
                                                    environment.readEnv(CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME_ENV),
                                                    environment.readEnv(CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE_ENV));
        return cristinUntisUtil.getAllData();
    }
}
