package no.unit.nva.cristin.projects.update;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.utils.ResourceAccessCheck;
import no.unit.nva.utils.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateProjectResourceAccessCheck implements ResourceAccessCheck<NvaProject> {

    private static final Logger logger = LoggerFactory.getLogger(UpdateProjectResourceAccessCheck.class);

    public static final String USER_IDENTIFIER = "userIdentifier";
    public static final String PROJECT_MANAGER = "ProjectManager";
    public static final String NOT_ALLOWED_TO_UPDATE_THIS_RESOURCE =
        "User with identifier: {} is not allowed to update resource with id {}";

    private transient boolean verified;

    @Override
    public void verifyAccess(NvaProject resource, Map<String, String> params) {
        var userIdentifier = params.get(USER_IDENTIFIER);

        var resourceCreator = getResourceCreator(resource);
        var resourceManager = getResourceManager(resource);

        if (resourceCreator.isPresent() && hasMatch(resourceCreator.get(), userIdentifier)) {
            verified = true;
            return;
        } else if (resourceManager.isPresent() && hasMatch(resourceManager.get(), userIdentifier)) {
            verified = true;
            return;
        }

        logger.info(NOT_ALLOWED_TO_UPDATE_THIS_RESOURCE, userIdentifier, resource.getId());

        verified = false;
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
                   .map(NvaContributor::getIdentity)
                   .map(Person::getId)
                   .map(UriUtils::extractLastPathElement)
                   .filter(Utils::isPositiveInteger);
    }

    private Optional<String> getResourceCreator(NvaProject resource) {
        return Optional.ofNullable(resource)
                   .map(NvaProject::getCreator)
                   .map(NvaContributor::getIdentity)
                   .map(Person::getId)
                   .map(UriUtils::extractLastPathElement)
                   .filter(Utils::isPositiveInteger);
    }

    private boolean isProjectManager(NvaContributor nvaContributor) {
        return PROJECT_MANAGER.equals(nvaContributor.getType());
    }

    private boolean hasMatch(String identifierFromResource, String userIdentifier) {
        return identifierFromResource.equals(userIdentifier);
    }
}
