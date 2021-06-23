package no.unit.nva.cristin.projects;

import nva.commons.apigateway.exceptions.ApiGatewayException;

public class NotAcceptableException extends ApiGatewayException {

    public NotAcceptableException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return 406;
    }
}
