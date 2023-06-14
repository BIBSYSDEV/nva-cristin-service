package no.unit.nva.utils;

import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface AccessCheck {

    void verifyAccess(RequestInfo requestInfo) throws ApiGatewayException;

}
