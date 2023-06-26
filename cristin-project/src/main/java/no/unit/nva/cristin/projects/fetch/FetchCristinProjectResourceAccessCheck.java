package no.unit.nva.cristin.projects.fetch;

import java.util.Map;
import no.unit.nva.cristin.projects.common.ProjectResourceAccessCheck;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.utils.ResourceAccessCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchCristinProjectResourceAccessCheck extends ProjectResourceAccessCheck
    implements ResourceAccessCheck<NvaProject> {

    private static final Logger logger = LoggerFactory.getLogger(FetchCristinProjectResourceAccessCheck.class);

    public static final String NOT_ALLOWED_TO_VIEW_THIS_RESOURCE =
        "User with identifier: {} is not allowed to view resource with id {}";

    private transient boolean verified;

    @Override
    public void verifyAccess(NvaProject resource, Map<String, String> params) {
        var userIdentifier = params.get(USER_IDENTIFIER);
        var resourceCreator = getResourceCreator(resource);

        if (resourceCreator.isPresent() && hasMatch(resourceCreator.get(), userIdentifier)) {
            verified = true;
            return;
        }

        logger.info(NOT_ALLOWED_TO_VIEW_THIS_RESOURCE, userIdentifier, resource.getId());

        verified = false;
    }

    @Override
    public boolean isVerified() {
        return verified;
    }
}
