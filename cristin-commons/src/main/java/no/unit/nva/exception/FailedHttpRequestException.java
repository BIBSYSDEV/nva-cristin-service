package no.unit.nva.exception;

import nva.commons.apigateway.exceptions.ApiGatewayException;

import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;

public class FailedHttpRequestException extends ApiGatewayException {


    public FailedHttpRequestException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return HTTP_BAD_GATEWAY;
    }
}
