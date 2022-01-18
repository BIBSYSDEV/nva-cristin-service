package no.unit.nva.utils;

import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ForbiddenException;

public class AccessUtils {

    public static final String EDIT_OWN_INSTITUTION_USERS = "EDIT_OWN_INSTITUTION_USERS";

    public static  void validateAccess(RequestInfo requestInfo) throws ForbiddenException {
        if (!requesterHasAccessToReadNationalIdentificationNumber(requestInfo)) {
            throw new ForbiddenException();
        }
    }

    private static boolean requesterHasAccessToReadNationalIdentificationNumber(RequestInfo requestInfo) {
        return requestInfo.getAccessRights().contains(EDIT_OWN_INSTITUTION_USERS);
    }

}
