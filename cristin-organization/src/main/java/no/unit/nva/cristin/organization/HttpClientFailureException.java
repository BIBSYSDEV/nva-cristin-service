package no.unit.nva.cristin.organization;

import nva.commons.apigateway.exceptions.ApiGatewayException;

import java.net.HttpURLConnection;

public class HttpClientFailureException extends ApiGatewayException {

    public static final int ERROR_CODE = HttpURLConnection.HTTP_BAD_GATEWAY;

    public HttpClientFailureException(String message) {
        super(message);
    }

    public HttpClientFailureException(Exception cause) {
        super(cause);
    }

    public HttpClientFailureException(Exception cause, String message) {
        super(cause, message);
    }

    @Override
    protected Integer statusCode() {
        return ERROR_CODE;
    }
}
