package no.unit.nva.access;

import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface HandlerAccessCheck {

    /**
     * Verifies if the user has access based on the request data.
     *
     * @param requestInfo data from the request used in access verification.
     * @throws ApiGatewayException access control exceptions
     **/
    void verifyAccess(RequestInfo requestInfo) throws ApiGatewayException;

    boolean isVerified();

}
