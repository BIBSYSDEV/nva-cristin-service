package no.unit.nva.exception;

import nva.commons.apigateway.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class InvalidUriException extends ApiGatewayException {

    public static final String MESSAGE_TEMPLATE = "The request URI <\"%s\"> cannot be parsed";

    public InvalidUriException(String uri) {
        super(String.format(MESSAGE_TEMPLATE, uri));
    }

    public InvalidUriException(Exception e, String uri) {
        super(e, String.format(MESSAGE_TEMPLATE, uri));
    }

    @Override
    protected Integer statusCode() {
        return HttpStatus.SC_BAD_REQUEST;
    }
}
