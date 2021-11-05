package no.unit.nva.cristin.organization.exception;

import nva.commons.apigateway.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class NonExistingUnitError extends ApiGatewayException {

    public static final String MESSAGE_TEMPLATE = "The URI \"%s\" cannot be dereferenced";

    public NonExistingUnitError(String uriString) {
        super(String.format(MESSAGE_TEMPLATE, uriString));
    }

    @Override
    protected Integer statusCode() {
        return HttpStatus.SC_BAD_GATEWAY;
    }
}
