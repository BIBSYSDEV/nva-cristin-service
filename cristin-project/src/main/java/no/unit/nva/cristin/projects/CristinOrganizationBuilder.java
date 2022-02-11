package no.unit.nva.cristin.projects;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import no.unit.nva.cristin.model.CristinInstitution;
import no.unit.nva.cristin.projects.model.cristin.CristinOrganization;
import no.unit.nva.cristin.projects.model.cristin.CristinUnit;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.UriUtils;

import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;

public class CristinOrganizationBuilder {

    public static final Pattern CRISTIN_UNIT_IDENTIFIER = Pattern.compile(ORGANIZATION_IDENTIFIER_PATTERN);

    private final transient Organization nvaOrganization;

    public CristinOrganizationBuilder(Organization organization) {
        this.nvaOrganization = organization;
    }

    /**
     * Build a CristinOrganization from given source.
     *
     * @return valid CristinOrganization containing data from source
     */
    public CristinOrganization build() {
        CristinInstitution cristinInstitution = new CristinInstitution();
        cristinInstitution.setCristinInstitutionId(extractLastPathElement(nvaOrganization.getId()));
        cristinInstitution.setUrl(nvaOrganization.getId().toString());
        cristinInstitution.setInstitutionName(nvaOrganization.getName());
        CristinOrganization cristinOrganization = new CristinOrganization();
        cristinOrganization.setInstitution(cristinInstitution);
        return cristinOrganization;
    }

    public static CristinOrganization fromUnitIdentifier(Organization organization) {
        return Optional.ofNullable(organization)
            .map(Organization::getId)
            .map(UriUtils::extractLastPathElement)
            .filter(isCristinUnitIdentifier())
            .map(CristinOrganizationBuilder::mapUnitIdentifierToCristinOrganization)
            .orElse(null);
    }

    private static Predicate<String> isCristinUnitIdentifier() {
        return identifier -> CRISTIN_UNIT_IDENTIFIER.matcher(identifier).matches();
    }

    private static CristinOrganization mapUnitIdentifierToCristinOrganization(String identifier) {
        CristinUnit unit = new CristinUnit();
        unit.setCristinUnitId(identifier);
        CristinOrganization cristinOrganization = new CristinOrganization();
        cristinOrganization.setInstitutionUnit(unit);
        return cristinOrganization;
    }
}
