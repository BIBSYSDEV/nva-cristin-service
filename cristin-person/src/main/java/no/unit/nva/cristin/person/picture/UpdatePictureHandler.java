package no.unit.nva.cristin.person.picture;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.util.Objects.isNull;
import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.person.model.nva.Binary;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class UpdatePictureHandler extends ApiGatewayHandler<Binary, Void> {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePictureHandler.class);
    public static final String STARTING_UPDATE_MESSAGE =
        "User is acting as themselves. Uploading profile picture to upstream";
    public static final String CANNOT_BE_EMPTY = "Json cannot be empty";
    public static final String NOT_AN_IMAGE = "Binary data is not an image";
    public static final String DECODED_DATA_SIZE_MESSAGE = "Decoded data is an image of size in bytes: {}";

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

        checkHasContent(input);
        var decoded = decodeInput(input);

        if (!isImage(decoded)) {
            throw new BadRequestException(NOT_AN_IMAGE);
        }

        logger.info(DECODED_DATA_SIZE_MESSAGE, decoded.length);

        return apiClient.uploadPicture(getValidPersonId(requestInfo), decoded);
    }

    private void checkHasContent(Binary input) throws BadRequestException {
        if (isNull(input.getBase64Data())) {
            throw new BadRequestException(CANNOT_BE_EMPTY);
        }
    }

    private byte[] decodeInput(Binary input) {
        return Base64.getDecoder().decode(input.getBase64Data());
    }

    private boolean isImage(byte[] decoded) {
        try (InputStream is = new ByteArrayInputStream(decoded)) {
            return ImageIO.read(is) != null;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Binary input, Void output) {
        return HTTP_NO_CONTENT;
    }

}
