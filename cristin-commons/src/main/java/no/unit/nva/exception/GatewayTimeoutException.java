package no.unit.nva.exception;

import java.net.HttpURLConnection;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public class GatewayTimeoutException extends ApiGatewayException {

    private static final long serialVersionUID = -23456235676891L;
    public static final String ERROR_MESSAGE_GATEWAY_TIMEOUT =
        "The request failed because the upstream server timed out";

    public GatewayTimeoutException() {
        this(ERROR_MESSAGE_GATEWAY_TIMEOUT);
    }

    public GatewayTimeoutException(String msg) {
        super(msg);
    }

    @Override
    protected Integer statusCode() {
        return HttpURLConnection.HTTP_GATEWAY_TIMEOUT;
    }
}
