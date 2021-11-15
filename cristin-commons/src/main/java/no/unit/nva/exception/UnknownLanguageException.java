package no.unit.nva.exception;

import nva.commons.apigateway.exceptions.ApiGatewayException;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

public class UnknownLanguageException extends ApiGatewayException {

    public static final int ERROR_CODE = HTTP_BAD_REQUEST;

    public UnknownLanguageException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return ERROR_CODE;
    }
}
