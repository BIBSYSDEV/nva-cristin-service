package no.unit.nva.cristin.projects.common;

import java.util.Optional;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.utils.UriUtils;

public class ProjectResourceAccessCheck {

    public static final String USER_IDENTIFIER = "userIdentifier";

    protected ProjectResourceAccessCheck() {

    }

    protected boolean hasMatch(String identifierFromResource, String userIdentifier) {
        return identifierFromResource.equals(userIdentifier);
    }

    protected Optional<String> getResourceCreator(NvaProject resource) {
        return Optional.ofNullable(resource)
                   .map(NvaProject::getCreator)
                   .map(NvaContributor::identity)
                   .map(Person::getId)
                   .map(UriUtils::extractLastPathElement)
                   .filter(Utils::isPositiveInteger);
    }

}
