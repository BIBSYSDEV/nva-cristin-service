package no.unit.nva.cristin.projects.create;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.Utils.isPositiveInteger;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import java.util.regex.Pattern;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.RequestInfo;

public class ProjectCreatorAppender {

    public static final Pattern ORGANIZATION_PATTERN = Pattern.compile(ORGANIZATION_IDENTIFIER_PATTERN);

    public static final String PROJECT_CREATOR = "ProjectCreator";

    private final NvaContributor creator;

    public ProjectCreatorAppender(RequestInfo requestInfo) {
        creator = extractProjectCreator(requestInfo);
    }

    private NvaContributor extractProjectCreator(RequestInfo requestInfo) {
        var clientIdentifier = extractCristinIdentifier(requestInfo);

        if (isNull(clientIdentifier) || !isPositiveInteger(clientIdentifier)) {
            return null;
        }

        var creator = new NvaContributor();
        creator.setIdentity(createIdentity(clientIdentifier));

        var clientOrganization = extractOrgIdentifier(requestInfo);
        if (nonNull(clientOrganization) && ORGANIZATION_PATTERN.matcher(clientOrganization).matches()) {
            creator.setAffiliation(createAffiliation(clientOrganization));
        }

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
