package no.unit.nva.cristin.projects.common;

import static no.unit.nva.utils.AccessUtils.EDIT_OWN_INSTITUTION_PROJECTS;
import static nva.commons.core.attempt.Try.attempt;
import no.unit.nva.utils.AccessCheck;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateProjectAccessCheck implements AccessCheck {

    private static final Logger logger = LoggerFactory.getLogger(CreateProjectAccessCheck.class);

    public static final String ACCESS_RIGHT_CREATE_PROJECT = "CREATE_PROJECT";
    public static final String USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT =
        "User:{} does not have required access right:{}";
    public static final String NO_USERNAME_FOUND = "NO USERNAME FOUND";

    @Override
    public void verifyAccess(RequestInfo requestInfo) throws ForbiddenException {
        if (!requestInfo.userIsAuthorized(ACCESS_RIGHT_CREATE_PROJECT) && !hasLegacyAccessRight(requestInfo)) {
            var username = attempt(requestInfo::getUserName).orElse(fail -> NO_USERNAME_FOUND);
            logger.info(USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT, username, ACCESS_RIGHT_CREATE_PROJECT);
            throw new ForbiddenException();
        }
    }

    private boolean hasLegacyAccessRight(RequestInfo requestInfo) {
        return requestInfo.userIsAuthorized(EDIT_OWN_INSTITUTION_PROJECTS);
    }
}
