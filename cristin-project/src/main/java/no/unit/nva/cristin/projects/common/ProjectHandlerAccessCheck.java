package no.unit.nva.cristin.projects.common;

import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_PROJECTS;
import nva.commons.apigateway.AccessRight;
import nva.commons.apigateway.RequestInfo;

public class ProjectHandlerAccessCheck {

    public static final String USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT =
        "User:{} does not have required access right:{}";
    public static final String NO_USERNAME_FOUND = "NO USERNAME FOUND";
    public static final String MANAGE_OWN_PROJECTS = AccessRight.MANAGE_OWN_PROJECTS.toString();

    protected ProjectHandlerAccessCheck() {
    }

    public static boolean hasLegacyAccessRight(RequestInfo requestInfo) {
        return requestInfo.userIsAuthorized(EDIT_OWN_INSTITUTION_PROJECTS);
    }

}
