package no.unit.nva.cristin.common.client;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static no.unit.nva.cristin.common.ErrorMessages.UPSTREAM_BAD_REQUEST_RESPONSE;
import static no.unit.nva.cristin.model.Constants.CRISTIN_INSTITUTION_HEADER;
import static no.unit.nva.cristin.model.Constants.CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME;
import static no.unit.nva.cristin.model.Constants.CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.exception.GatewayTimeoutException;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;

public class PatchApiClient extends ApiClient {

    public static final String EMPTY_JSON = "{}";
    public static final String HTTP_METHOD_PATCH = "PATCH";
    public static final String APPLICATION_MERGE_PATCH_JSON = "application/merge-patch+json";

    public PatchApiClient(HttpClient client) {
        super(client);
    }

    /**
     * Initiate a synchronous PATCH to uri with supplied payload. Returns response from upstream.
     */
    public HttpResponse<String> patch(URI uri, String body)
        throws GatewayTimeoutException, FailedHttpRequestException {

        HttpRequest httpRequest = HttpRequest.newBuilder()
                                      .uri(uri)
                                      .header(CONTENT_TYPE, APPLICATION_MERGE_PATCH_JSON)
                                      .header(CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME,
                                              CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE)
                                      .method(HTTP_METHOD_PATCH, HttpRequest.BodyPublishers.ofString(body))
                                      .build();
        return getSuccessfulResponseOrThrowException(httpRequest);
    }

    /**
     * Initiate a synchronous PATCH to uri with supplied payload. Returns response from upstream.
     * Accepts header for Cristin Institution allowed to update.
     */
    public HttpResponse<String> patch(URI uri, String body, String cristinInstitutionNumber)
        throws GatewayTimeoutException, FailedHttpRequestException {

        HttpRequest httpRequest = HttpRequest.newBuilder()
                                      .uri(uri)
                                      .header(CONTENT_TYPE, APPLICATION_MERGE_PATCH_JSON)
                                      .header(CRISTIN_INSTITUTION_HEADER, cristinInstitutionNumber)
                                      .header(CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME,
                                              CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE)
                                      .method(HTTP_METHOD_PATCH, HttpRequest.BodyPublishers.ofString(body))
                                      .build();
        return getSuccessfulResponseOrThrowException(httpRequest);
    }

    /**
     * Checks for PATCH http error codes and also the regular error codes. Throws exception on error
     */
    protected void checkPatchHttpStatusCode(URI uri, int statusCode, String body) throws ApiGatewayException {
        if (isSuccessful(statusCode)) {
            return;
        }
        if (statusCode == HTTP_BAD_REQUEST) {
            throw new BadRequestException(UPSTREAM_BAD_REQUEST_RESPONSE + body);
        }
        checkHttpStatusCode(uri, statusCode);
    }
}
