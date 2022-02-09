package no.unit.nva.cristin.projects;

import java.util.Optional;
import java.util.regex.Pattern;
import no.unit.nva.cristin.projects.model.cristin.CristinOrganization;
import no.unit.nva.cristin.projects.model.cristin.CristinUnit;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.UriUtils;

import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;

public class CristinOrganizationBuilder {

    public static final Pattern UNIT_ID_PATTERN = Pattern.compile(ORGANIZATION_IDENTIFIER_PATTERN);

    /**
     * Build a CristinOrganization containing a CristinInstitution.
     *
     * @return valid CristinOrganization containing data from source
     */
    public static CristinOrganization buildWithCristinInstitution(Organization organization) {
        CristinOrganization cristinOrganization = new CristinOrganization();
        cristinOrganization.setInstitution(organization.toCristinInstitution());
        return cristinOrganization;
    }

    /**
     * Build a CristinOrganization containing a CristinUnit.
     *
     * @return valid CristinOrganization containing data from source
     */
    public static CristinOrganization fromUnitIdentifier(Organization organization) {
        return Optional.ofNullable(organization)
            .map(Organization::getId)
            .map(UriUtils::extractLastPathElement)
            .filter(identifier -> UNIT_ID_PATTERN.matcher(identifier).matches())
            .map(CristinOrganizationBuilder::mapUnitIdentifierToCristinOrganization)
            .orElse(null);
    }

    private static CristinOrganization mapUnitIdentifierToCristinOrganization(String identifier) {
        CristinUnit unit = new CristinUnit();
        unit.setCristinUnitId(identifier);
        CristinOrganization cristinOrganization = new CristinOrganization();
        cristinOrganization.setInstitutionUnit(unit);
        return cristinOrganization;
    }
}
