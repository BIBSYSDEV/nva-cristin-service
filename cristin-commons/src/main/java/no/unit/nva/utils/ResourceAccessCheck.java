package no.unit.nva.utils;

import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface ResourceAccessCheck<T> {

    void verifyAccess(T resource) throws ApiGatewayException;

}
