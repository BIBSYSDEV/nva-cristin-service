package no.unit.nva.cristin.projects.create;

import static nva.commons.core.attempt.Try.attempt;
import no.unit.nva.cristin.projects.common.ProjectHandlerAccessCheck;
import no.unit.nva.utils.HandlerAccessCheck;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateProjectHandlerAccessCheck extends ProjectHandlerAccessCheck implements HandlerAccessCheck {

    private static final Logger logger = LoggerFactory.getLogger(CreateProjectHandlerAccessCheck.class);

    public static final String ACCESS_RIGHT_CREATE_PROJECT = "CREATE_PROJECT";

    @Override
    public void verifyAccess(RequestInfo requestInfo) throws ForbiddenException {
        if (!requestInfo.userIsAuthorized(ACCESS_RIGHT_CREATE_PROJECT) && hasNoLegacyAccessRight(requestInfo)) {
            var username = attempt(requestInfo::getUserName).orElse(fail -> NO_USERNAME_FOUND);
            logger.info(USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT, username, ACCESS_RIGHT_CREATE_PROJECT);
            throw new ForbiddenException();
        }
    }
}
