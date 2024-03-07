package no.unit.nva.cristin.keyword.create;

import static nva.commons.apigateway.AccessRight.MANAGE_OWN_RESOURCES;
import no.unit.nva.access.HandlerAccessCheck;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.ForbiddenException;

public class CreateKeywordHandlerAccessCheck implements HandlerAccessCheck {

    private boolean verified;

    @Override
    public void verifyAccess(RequestInfo requestInfo) throws ApiGatewayException {
        verified = requestInfo.userIsAuthorized(MANAGE_OWN_RESOURCES);
        if (!verified) {
            throw new ForbiddenException();
        }
    }

    @Override
    public boolean isVerified() {
        return verified;
    }
}
