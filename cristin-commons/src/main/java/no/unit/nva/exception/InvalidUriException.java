package no.unit.nva.exception;

import nva.commons.apigateway.exceptions.ApiGatewayException;

import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;

public class InvalidUriException extends ApiGatewayException {

    private static final long serialVersionUID = -23453456491L;
    public static final String MESSAGE_TEMPLATE = "The request URI <\"%s\"> cannot be parsed";

    public InvalidUriException(String uri) {
        super(String.format(MESSAGE_TEMPLATE, uri));
    }

    public InvalidUriException(Exception e, String uri) {
        super(e, String.format(MESSAGE_TEMPLATE, uri));
    }

    @Override
    protected Integer statusCode() {
        return HTTP_BAD_GATEWAY;
    }
}
