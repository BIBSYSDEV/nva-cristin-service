package no.unit.nva.exception;

import nva.commons.apigateway.exceptions.ApiGatewayException;
import org.apache.http.HttpStatus;

public class UnknownLanguageException extends ApiGatewayException {

    public static final int ERROR_CODE = HttpStatus.SC_BAD_REQUEST;

    public UnknownLanguageException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return ERROR_CODE;
    }
}
