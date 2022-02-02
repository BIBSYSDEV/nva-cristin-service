package no.unit.nva.exception;

import nva.commons.apigateway.exceptions.ApiGatewayException;

import java.net.HttpURLConnection;

public class UnauthorizedException extends ApiGatewayException {
    public static final String DEFAULT_MESSAGE = "Unauthorized";

    public UnauthorizedException() {
        super(DEFAULT_MESSAGE);
    }

    @Override
    protected Integer statusCode() {
        return HttpURLConnection.HTTP_UNAUTHORIZED;
    }

}
