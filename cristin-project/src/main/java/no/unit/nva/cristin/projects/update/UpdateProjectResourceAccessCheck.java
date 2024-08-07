package no.unit.nva.cristin.projects.update;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.projects.common.ProjectResourceAccessCheck;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.access.ResourceAccessCheck;
import no.unit.nva.utils.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateProjectResourceAccessCheck extends ProjectResourceAccessCheck
    implements ResourceAccessCheck<NvaProject> {

    private static final Logger logger = LoggerFactory.getLogger(UpdateProjectResourceAccessCheck.class);

    public static final String PROJECT_MANAGER_ROLE_CODE = "ProjectManager";
    public static final String NOT_ALLOWED_TO_UPDATE_THIS_RESOURCE =
        "User with identifier: {} is not allowed to update resource with id {}";

    private transient boolean verified;

    
    @Override
    public void verifyAccess(NvaProject resource, Map<String, String> params) {
        var userIdentifier = params.get(USER_IDENTIFIER);

        getResourceCreator(resource)
            .ifPresentOrElse(creator -> verified = hasMatch(creator, userIdentifier),
                             () -> verified = false);
        if (verified) {
            return;
        }

        getResourceManager(resource)
            .ifPresent(manager -> verified = hasMatch(manager, userIdentifier));
        if (verified) {
            return;
        }

        logger.info(NOT_ALLOWED_TO_UPDATE_THIS_RESOURCE, userIdentifier, resource.getId());
    }

    @Override
    public boolean isVerified() {
        return verified;
    }

    private Optional<String> getResourceManager(NvaProject resource) {
        return Optional.ofNullable(resource)
                   .map(NvaProject::getContributors)
                   .stream()
                   .flatMap(Collection::stream)
                   .filter(this::isProjectManager)
                   .findAny()
                   .map(NvaContributor::identity)
                   .map(Person::getId)
                   .map(UriUtils::extractLastPathElement)
                   .filter(Utils::isPositiveInteger);
    }

    private boolean isProjectManager(NvaContributor nvaContributor) {
        return nvaContributor.roles()
                   .stream()
                   .anyMatch(role -> PROJECT_MANAGER_ROLE_CODE.equals(role.type()));
    }

}
