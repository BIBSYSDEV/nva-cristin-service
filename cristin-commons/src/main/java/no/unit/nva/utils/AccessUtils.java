package no.unit.nva.utils;

import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ForbiddenException;

public class AccessUtils {

    public static final String EDIT_OWN_INSTITUTION_USERS = "EDIT_OWN_INSTITUTION_USERS";

    /**
     * Validate Requesters access to a resource.
     * @param requestInfo information from request
     * @throws ForbiddenException thrown when user has no access to requested resource
     */
    public static  void validateAccess(RequestInfo requestInfo) throws ForbiddenException {
        if (!requesterHasAccessRightToUseNationalIdentificationNumber(requestInfo)) {
            throw new ForbiddenException();
        }
    }

    private static boolean requesterHasAccessRightToUseNationalIdentificationNumber(RequestInfo requestInfo) {
        return requestInfo.getAccessRights().contains(EDIT_OWN_INSTITUTION_USERS);
    }

}
