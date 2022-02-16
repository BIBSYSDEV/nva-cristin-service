package no.unit.nva.utils;

import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessUtils {

    public static final String EDIT_OWN_INSTITUTION_USERS = "EDIT_OWN_INSTITUTION_USERS";
    public static final String EDIT_OWN_INSTITUTION_PROJECTS = "EDIT_OWN_INSTITUTION_PROJECTS";

    private static final Logger logger = LoggerFactory.getLogger(AccessUtils.class);
    public static final String USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT =
            "User:{} does not have required access right:{}";

    /**
     * Validate if Requester is authorized to use IdentificationNumber to a access a user.
     *
     * @param requestInfo information from request
     * @throws ForbiddenException thrown when user is not authorized to access a user with IdentificationNumber
     */
    public static void validateIdentificationNumberAccess(RequestInfo requestInfo) throws ForbiddenException {
        if (requesterHasNoAccessRightToUseNationalIdentificationNumber(requestInfo)) {
            logger.warn(USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT,
                    requestInfo.getFeideId(), EDIT_OWN_INSTITUTION_USERS);
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
            logger.warn(USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT,
                    requestInfo.getFeideId(), EDIT_OWN_INSTITUTION_PROJECTS);
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
