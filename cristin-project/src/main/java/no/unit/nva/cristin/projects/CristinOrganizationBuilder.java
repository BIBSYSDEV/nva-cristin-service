package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.model.CristinInstitution;
import no.unit.nva.cristin.projects.model.cristin.CristinOrganization;
import no.unit.nva.model.Organization;

import static no.unit.nva.cristin.projects.CristinProjectBuilder.extractLastPathElement;

public class CristinOrganizationBuilder {

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
}
