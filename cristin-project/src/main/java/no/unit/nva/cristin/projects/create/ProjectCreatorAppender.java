package no.unit.nva.cristin.projects.create;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.RequestInfo;

public class ProjectCreatorAppender {

    public static final String PROJECT_CREATOR = "ProjectCreator";

    private final NvaContributor creator;

    public ProjectCreatorAppender(RequestInfo requestInfo) {
        creator = extractProjectCreator(requestInfo);
    }

    private NvaContributor extractProjectCreator(RequestInfo requestInfo) {
        var clientIdentifier = extractCristinIdentifier(requestInfo);
        var clientOrganization = extractOrgIdentifier(requestInfo);

        if (isNull(clientIdentifier) || isNull(clientOrganization)) {
            return null;
        }

        var creator = new NvaContributor();
        creator.setIdentity(createIdentity(clientIdentifier));
        creator.setAffiliation(createAffiliation(clientOrganization));
        creator.setType(PROJECT_CREATOR);

        return creator;
    }

    private Organization createAffiliation(String identifier) {
        var affiliation = getNvaApiId(identifier, ORGANIZATION_PATH);
        return new Organization.Builder().withId(affiliation).build();
    }

    private Person createIdentity(String identifier) {
        var identity = getNvaApiId(identifier, PERSON_PATH_NVA);
        return new Person.Builder().withId(identity).build();
    }

    public NvaContributor getCreator() {
        return creator;
    }
}
