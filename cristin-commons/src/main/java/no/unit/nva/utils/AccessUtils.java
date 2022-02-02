package no.unit.nva.utils;

import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ForbiddenException;

public class AccessUtils {

    public static final String EDIT_OWN_INSTITUTION_USERS = "EDIT_OWN_INSTITUTION_USERS";
    public static final String EDIT_OWN_INSTITUTION_PROJECTS = "EDIT_OWN_INSTITUTION_PROJECTS";

    /**
     * Validate if Requester is authorized to use IdentificationNumber to a access a user.
     *
     * @param requestInfo information from request
     * @throws ForbiddenException thrown when user is not authorized to access a user with IdentificationNumber
     */
    public static void validateIdentificationNumberAccess(RequestInfo requestInfo) throws ForbiddenException {
        if (requesterHasNoAccessRightToUseNationalIdentificationNumber(requestInfo)) {
            throw new ForbiddenException();
        }
    }

    /**
     * Validate if Requester is authorized to create or change a project.
     *
     * @param requestInfo information from request
     * @throws ForbiddenException thrown when user has no access to create or change projects
     */
    public static void verifyRequesterCanEditProjects(RequestInfo requestInfo) throws ForbiddenException {
        if (requesterHasNoAccessRightToEditProjects(requestInfo)) {
            throw new ForbiddenException();
        }
    }

    private static boolean requesterHasNoAccessRightToUseNationalIdentificationNumber(RequestInfo requestInfo) {
        return !requestInfo.getAccessRights().contains(EDIT_OWN_INSTITUTION_USERS);
    }

    private static boolean requesterHasNoAccessRightToEditProjects(RequestInfo requestInfo) {
        return !requestInfo.getAccessRights().contains(EDIT_OWN_INSTITUTION_PROJECTS);
    }

}
