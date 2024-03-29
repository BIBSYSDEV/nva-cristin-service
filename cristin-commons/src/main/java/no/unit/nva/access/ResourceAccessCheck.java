package no.unit.nva.access;

import java.util.Map;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface ResourceAccessCheck<T> {

    /**
     * Verifies if the user has access based on the resource requested access to.
     *
     * @param resource the resource to use in access verification.
     * @param params params needed for verification.
     * @throws ApiGatewayException access control exceptions
     **/
    void verifyAccess(T resource, Map<String, String> params) throws ApiGatewayException;

    boolean isVerified();

}
