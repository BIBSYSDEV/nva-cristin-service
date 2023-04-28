package no.unit.nva.cristin.person.picture;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_UNSUPPORTED_TYPE;
import static no.unit.nva.cristin.common.ErrorMessages.UPSTREAM_BAD_REQUEST_RESPONSE;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME;
import static no.unit.nva.cristin.model.Constants.CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import no.unit.nva.cristin.common.client.ApiClient;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PictureApiClient extends ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(PictureApiClient.class);

    public static final String PICTURE_PATH = "picture";
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String IDENTIFIER_OR_PAYLOAD_IS_NOT_VALID = "Supplied identifier or payload is not valid";
    public static final String UNSUPPORTED_MEDIA_TYPE_ERROR_MESSAGE = "Upstream returned unsupported media type";
    public static final String PAYLOAD_NOT_ACCEPTED_BY_UPSTREAM = "Payload not accepted by upstream";
    public static final String ERROR_SENDING_BINARY_TO_UPSTREAM = "Error sending profile picture to upstream";
    public static final String DELIMITER = " : ";

    public PictureApiClient(HttpClient client) {
        super(client);
    }

    /**
     * Updates a person's profile picture in Cristin.
     *
     * @return An empty json if update was successful
     * @throws ApiGatewayException if something went wrong that can be mapped to a client response
     */
    public Void uploadPicture(String personId, byte[] input) throws ApiGatewayException {
        var uri = generateCristinUri(personId);
        try (InputStream stream = new ByteArrayInputStream(input)) {
            var response = put(uri, stream);
            checkPutHttpStatusCode(generateIdUri(personId), response.statusCode(), response.body());
        } catch (IOException ex) {
            logger.error(ERROR_SENDING_BINARY_TO_UPSTREAM + DELIMITER + ex.getMessage());
            throw new BadGatewayException(ERROR_SENDING_BINARY_TO_UPSTREAM);
        }

        return null;
    }

    /**
     * Build and perform blocking synchronous PUT request for given URI.
     * @param uri to call
     * @return response containing data from requested URI or error
     */
    private HttpResponse<String> put(URI uri, InputStream body) throws ApiGatewayException {
        var httpRequest = HttpRequest.newBuilder(uri)
                              .header(CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME, CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE)
                              .header(CONTENT_TYPE, IMAGE_JPEG)
                              .PUT(BodyPublishers.ofInputStream(() -> body))
                              .build();
        return getSuccessfulResponseOrThrowException(httpRequest);
    }

    /**
     * Checks for PATCH http error codes and also the regular error codes. Throws exception on error
     */
    private void checkPutHttpStatusCode(URI uri, int statusCode, String body) throws ApiGatewayException {
        if (isSuccessful(statusCode)) {
            return;
        }
        if (statusCode == HTTP_BAD_REQUEST) {
            logger.error(UPSTREAM_BAD_REQUEST_RESPONSE + body);
            throw new BadRequestException(IDENTIFIER_OR_PAYLOAD_IS_NOT_VALID);
        }
        if (statusCode == HTTP_UNSUPPORTED_TYPE) {
            logger.error(UNSUPPORTED_MEDIA_TYPE_ERROR_MESSAGE);
            throw new BadRequestException(PAYLOAD_NOT_ACCEPTED_BY_UPSTREAM);
        }
        checkHttpStatusCode(uri, statusCode, body);
    }

    private URI generateCristinUri(String personId) {
        return  UriWrapper.fromUri(CRISTIN_API_URL).addChild(PERSON_PATH).addChild(personId)
                    .addChild(PICTURE_PATH).getUri();
    }

    private URI generateIdUri(String personId) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(PERSON_PATH_NVA).addChild(personId)
                   .addChild(PICTURE_PATH).getUri();
    }

}
