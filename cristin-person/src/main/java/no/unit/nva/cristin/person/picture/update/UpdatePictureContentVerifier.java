package no.unit.nva.cristin.person.picture.update;

import static java.util.Objects.isNull;
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

    public static final String CANNOT_BE_EMPTY = "Json cannot be empty";
    public static final String NOT_AN_IMAGE = "Binary data is not an image";
    public static final String DECODED_DATA_SIZE_MESSAGE = "Decoded data is an image of size in bytes: {}";

    private final byte[] decoded;

    /**
    * Verifies that content from input is a valid image and decodes it.
    **/
    public UpdatePictureContentVerifier(Binary input) throws BadRequestException {
        checkHasContent(input);
        decoded = decodeInput(input);

        if (!isImage(decoded)) {
            throw new BadRequestException(NOT_AN_IMAGE);
        }

        logger.info(DECODED_DATA_SIZE_MESSAGE, decoded.length);
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

    public byte[] getDecoded() {
        return decoded.clone();
    }
}
