package no.unit.nva.exception;

import nva.commons.apigateway.exceptions.ApiGatewayException;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

public class NonExistingUnitError extends ApiGatewayException {

    public static final String MESSAGE_TEMPLATE = "The URI \"%s\" cannot be dereferenced";

    public NonExistingUnitError(String uriString) {
        super(String.format(MESSAGE_TEMPLATE, uriString));
    }

    @Override
    protected Integer statusCode() {
        return HTTP_NOT_FOUND;
    }
}
