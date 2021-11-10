package no.unit.nva.cristin.organization.exception;

import nva.commons.apigateway.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class FailedHttpRequestException extends ApiGatewayException {

    public static Integer ERROR_CODE = HttpStatus.SC_BAD_GATEWAY;

    public FailedHttpRequestException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return ERROR_CODE;
    }
}
