package no.unit.nva.cristin.person.picture.fetch;

import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.cristin.common.Utils.getValidIdentifier;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.Map;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;

public class FetchPictureHandler extends ApiGatewayHandler<Void, byte[]> {

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String IMAGE_JPEG = "image/jpeg";
    private final transient FetchPictureApiClient apiClient;

    @SuppressWarnings("unused")
    public FetchPictureHandler() {
        this(new FetchPictureApiClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    public FetchPictureHandler(FetchPictureApiClient apiClient, Environment environment) {
        super(Void.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected byte[] processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        var identifier = getValidIdentifier(requestInfo);

        addAdditionalHeaders(() -> Map.of(CONTENT_TYPE, IMAGE_JPEG));

        return apiClient.fetchPicture(identifier);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, byte[] output) {
        return HTTP_OK;
    }
}
