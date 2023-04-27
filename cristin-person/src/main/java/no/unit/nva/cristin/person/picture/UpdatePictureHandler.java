package no.unit.nva.cristin.person.picture;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;
import com.amazonaws.services.lambda.runtime.Context;
import java.util.Base64;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.person.model.nva.Binary;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class UpdatePictureHandler extends ApiGatewayHandler<Binary, Void> {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePictureHandler.class);
    public static final String STARTING_UPDATE_MESSAGE =
        "User is acting as themselves. Uploading profile picture to upstream";

    private final transient PictureApiClient apiClient;

    public UpdatePictureHandler() {
        this(new PictureApiClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    public UpdatePictureHandler(PictureApiClient apiClient, Environment environment) {
        super(Binary.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Void processInput(Binary input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        new UpdatePictureAccessCheck().verifyAccess(requestInfo);

        logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));
        logger.info(STARTING_UPDATE_MESSAGE);

        verifyInput(input);

        return apiClient.uploadPicture(getValidPersonId(requestInfo), input);
    }

    private void verifyInput(Binary input) {
        var decoded = Base64.getDecoder().decode(input.getBase64Data());
        logger.info("Decoded data is an image of size in bytes: {}", decoded.length);
    }

    @Override
    protected Integer getSuccessStatusCode(Binary input, Void output) {
        return HTTP_NO_CONTENT;
    }

}
