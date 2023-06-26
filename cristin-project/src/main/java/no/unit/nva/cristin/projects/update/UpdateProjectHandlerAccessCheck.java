package no.unit.nva.cristin.projects.update;

import static nva.commons.core.attempt.Try.attempt;
import no.unit.nva.cristin.projects.common.ProjectHandlerAccessCheck;
import no.unit.nva.utils.HandlerAccessCheck;
import nva.commons.apigateway.RequestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateProjectHandlerAccessCheck extends ProjectHandlerAccessCheck implements HandlerAccessCheck {

    private static final Logger logger = LoggerFactory.getLogger(UpdateProjectHandlerAccessCheck.class);

    private transient boolean verified;

    @Override
    public void verifyAccess(RequestInfo requestInfo) {
        if (requestInfo.userIsAuthorized(MANAGE_OWN_PROJECTS)) {
            verified = true;
        } else {
            var username = attempt(requestInfo::getUserName).orElse(fail -> NO_USERNAME_FOUND);
            logger.info(USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT, username, MANAGE_OWN_PROJECTS);
            verified = false;
        }
    }

    @Override
    public boolean isVerified() {
        return verified;
    }

}
