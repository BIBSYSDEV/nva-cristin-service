package no.unit.nva.cristin.person.picture.fetch;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FAILED_WITH_EXCEPTION;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_BACKEND_FETCH_FAILED;
import static no.unit.nva.cristin.common.client.CristinAuthenticator.basicAuthHeader;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME;
import static no.unit.nva.cristin.model.Constants.CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.exception.FailedHttpRequestException;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchPictureApiClient extends ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(FetchPictureApiClient.class);

    public static final String PICTURE_PATH = "picture";

    private final transient HttpClient httpClient;

    public FetchPictureApiClient(HttpClient httpClient) {
        super(httpClient);
        this.httpClient = httpClient;
    }

    /**
     * Retrieves a person's profile picture in Cristin.
     *
     * @return A byte[] containing person profile picture
     * @throws ApiGatewayException if something went wrong that can be mapped to a client response
     */
    public byte[] fetchPicture(String personId) throws ApiGatewayException {
        var uri = generateCristinUri(personId);
        var response = fetchBinary(uri);
        checkHttpStatusCode(generateIdUri(personId), response.statusCode());

        return response.body();
    }

    /**
     * Build and perform blocking synchronous GET request for given URI.
     * @param uri to call
     * @return response containing data from requested URI or error
     */
    private HttpResponse<byte[]> fetchBinary(URI uri) throws ApiGatewayException {
        var httpRequest = HttpRequest.newBuilder(uri)
                              .header(CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME, CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE)
                              .header(AUTHORIZATION, readBasicAuthHeader())
                              .GET()
                              .build();
        return getSuccessfulBinaryResponseOrThrowException(httpRequest);
    }

    protected String readBasicAuthHeader() {
        return basicAuthHeader();
    }

    private URI generateCristinUri(String personId) {
        return  UriWrapper.fromUri(CRISTIN_API_URL).addChild(PERSON_PATH).addChild(personId)
                    .addChild(PICTURE_PATH).getUri();
    }

    private URI generateIdUri(String personId) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(PERSON_PATH_NVA).addChild(personId)
                   .addChild(PICTURE_PATH).getUri();
    }

    private HttpResponse<byte[]> getSuccessfulBinaryResponseOrThrowException(HttpRequest httpRequest)
        throws FailedHttpRequestException {

        try {
            return httpClient.send(httpRequest, BodyHandlers.ofByteArray());
        } catch (Exception exception) {
            var uri = httpRequest.uri().toString();
            logError(uri, exception);
            throw new FailedHttpRequestException(ERROR_MESSAGE_BACKEND_FETCH_FAILED);
        }
    }

    private void logError(String uri, Exception failure) {
        logger.error(String.format(ERROR_MESSAGE_BACKEND_FAILED_WITH_EXCEPTION, uri,
                                   failure.getClass().getCanonicalName() + ": " + failure.getMessage()));
    }

    private void checkHttpStatusCode(URI uri, int statusCode) throws ApiGatewayException {
        checkHttpStatusCode(uri, statusCode, null);
    }

}
