package no.unit.nva.cristin.model;

import java.util.Optional;
import no.unit.nva.model.Organization;

import static no.unit.nva.cristin.model.CristinInstitution.fromOrganization;
import static no.unit.nva.cristin.model.CristinUnit.extractUnitIdentifier;

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
        return extractUnitIdentifier(organization).map(CristinOrganization::fromIdentifier);
    }

}
