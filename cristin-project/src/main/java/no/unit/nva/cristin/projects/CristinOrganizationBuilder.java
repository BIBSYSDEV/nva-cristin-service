package no.unit.nva.cristin.projects;

import java.util.Optional;
import no.unit.nva.cristin.projects.model.cristin.CristinOrganization;
import no.unit.nva.model.Organization;

import static no.unit.nva.cristin.model.CristinInstitution.fromOrganization;
import static no.unit.nva.cristin.projects.model.cristin.CristinUnit.extractUnitIdentifier;
import static no.unit.nva.cristin.projects.model.cristin.CristinUnit.fromCristinUnitIdentifier;

public class CristinOrganizationBuilder {

    /**
     * Create a CristinOrganization from Organization containing an CristinInstitution.
     *
     * @return valid CristinOrganization containing data from CristinInstitution
     */
    public static CristinOrganization fromOrganizationContainingInstitution(Organization organization) {
        CristinOrganization cristinOrganization = new CristinOrganization();
        cristinOrganization.setInstitution(fromOrganization(organization));
        return cristinOrganization;
    }

    /**
     * Create a CristinOrganization from Organization containing a CristinUnit if present.
     *
     * @return valid Optional CristinOrganization containing data from CristinUnit or else empty
     */
    public static Optional<CristinOrganization> fromOrganizationContainingUnitIfPresent(Organization organization) {
        return extractUnitIdentifier(organization)
            .map(CristinOrganizationBuilder::mapUnitIdentifierToCristinOrganization);
    }

    private static CristinOrganization mapUnitIdentifierToCristinOrganization(String unitIdentifier) {
        CristinOrganization cristinOrganization = new CristinOrganization();
        cristinOrganization.setInstitutionUnit(fromCristinUnitIdentifier(unitIdentifier));
        return cristinOrganization;
    }
}
