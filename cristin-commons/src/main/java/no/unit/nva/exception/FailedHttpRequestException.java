package no.unit.nva.exception;

import nva.commons.apigateway.exceptions.ApiGatewayException;

import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;

public class FailedHttpRequestException extends ApiGatewayException {

    private static final long serialVersionUID = -2345623546891L;

    public FailedHttpRequestException(String message) {
        super(message);
    }

    public FailedHttpRequestException(Exception e, String message) {
        super(e, message);
    }

    public FailedHttpRequestException(Exception e) {
        super(e);
    }

    public FailedHttpRequestException(Exception e, Integer statusCode) {
        super(e, statusCode);
    }

    @Override
    protected Integer statusCode() {
        return HTTP_BAD_GATEWAY;
    }
}
