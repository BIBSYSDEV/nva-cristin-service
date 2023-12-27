package no.unit.nva.cristin.projects.common;

import nva.commons.apigateway.AccessRight;

public class ProjectHandlerAccessCheck {

    public static final String USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT =
        "User:{} does not have required access right:{}";
    public static final String NO_USERNAME_FOUND = "NO USERNAME FOUND";
    public static final AccessRight MANAGE_OWN_PROJECTS = AccessRight.MANAGE_OWN_PROJECTS;

    protected ProjectHandlerAccessCheck() {
    }

}
