package no.unit.nva.cristin.person.picture.update;

import static java.util.Objects.nonNull;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import no.unit.nva.cristin.person.model.nva.Binary;
import nva.commons.apigateway.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdatePictureContentVerifier {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePictureContentVerifier.class);

    public static final String NOT_AN_IMAGE = "Binary data is not an image";
    public static final String DECODED_DATA_SIZE_MESSAGE = "Decoded data is an image of size in bytes: {}";
    public static final String SENT_EMPTY_PAYLOAD =
        "Client sent an empty payload, attempting to delete profile picture";
    public static final String COULD_NOT_DECODE_BINARY_DATA = "Could not decode binary data";

    private final byte[] decoded;

    /**
    * Verifies that content from input is a valid image and decodes it.
    **/
    public UpdatePictureContentVerifier(Binary input) throws BadRequestException {
        if (!hasContent(input)) {
            decoded = decodeInput(emptyPayload());
            logger.info(SENT_EMPTY_PAYLOAD);
            return;
        }

        try {
            decoded = decodeInput(input);
        } catch (Exception ex) {
            logger.info(COULD_NOT_DECODE_BINARY_DATA);
            throw new BadRequestException(NOT_AN_IMAGE);
        }

        if (!isImage(decoded)) {
            throw new BadRequestException(NOT_AN_IMAGE);
        }

        logger.info(DECODED_DATA_SIZE_MESSAGE, decoded.length);
    }

    private boolean hasContent(Binary input) {
        return nonNull(input.getBase64Data());
    }

    private static Binary emptyPayload() {
        return new Binary(EMPTY_STRING);
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

    public byte[] getDecoded() {
        return decoded.clone();
    }
}
