package no.unit.nva;

import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface Validator<T> {

    void validate(T classOfT) throws ApiGatewayException;

}
