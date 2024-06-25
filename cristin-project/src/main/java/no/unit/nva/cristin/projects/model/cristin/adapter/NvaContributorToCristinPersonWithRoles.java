package no.unit.nva.cristin.projects.model.cristin.adapter;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.model.CristinInstitution.fromOrganization;
import static no.unit.nva.cristin.model.CristinUnit.extractUnitIdentifier;
import static no.unit.nva.cristin.projects.model.nva.ContributorRoleMapping.getCristinRole;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.model.CristinRole;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;

public class NvaContributorToCristinPersonWithRoles implements Function<NvaContributor, CristinPerson> {

    private NvaContributor nvaContributor;

    @Override
    public CristinPerson apply(NvaContributor nvaContributor) {
        if (isNull(nvaContributor)) {
            return null;
        }
        this.nvaContributor = nvaContributor;

        return toCristinPersonWithRoles();
    }

    /**
     * Create a CristinPerson from identity with added roles.
     *
     * @return a CristinPerson from identity and roles
     */
    public CristinPerson toCristinPersonWithRoles() {
        var cristinPerson = new PersonToCristinPersonWithoutRoles().apply(nvaContributor.getIdentity());

        return cristinPerson.copy()
                   .withRoles(extractCristinRoles())
                   .build();
    }

    private List<CristinRole> extractCristinRoles() {
        return contributorTypeCanBeMappedToCristinRole() ? getCristinRoles() : Collections.emptyList();
    }

    private boolean contributorTypeCanBeMappedToCristinRole() {
        return getCristinRole(nvaContributor.getType()).isPresent();
    }

    private List<CristinRole> getCristinRoles() {
        var cristinRole = new CristinRole();
        addRolesBasedOnContributorsType(cristinRole);
        addOrganizationInformationToCristinRole(cristinRole);

        return List.of(cristinRole);
    }

    private void addOrganizationInformationToCristinRole(CristinRole cristinRole) {
        if (contributorHasValidUnitIdentifier()) {
            var unitIdentifier = extractUnitIdentifier(nvaContributor.getAffiliation()).orElseThrow();
            cristinRole.setInstitutionUnit(new CristinUnit(unitIdentifier));
        } else {
            var defaultOrganization = fromOrganization(nvaContributor.getAffiliation());
            cristinRole.setInstitution(defaultOrganization);
        }
    }

    private void addRolesBasedOnContributorsType(CristinRole cristinRole) {
        if (contributorTypeCanBeMappedToCristinRole()) {
            var cristinRoleCode = getCristinRole(nvaContributor.getType()).orElseThrow();
            cristinRole.setRoleCode(cristinRoleCode);
        }
    }

    private boolean contributorHasValidUnitIdentifier() {
        return extractUnitIdentifier(nvaContributor.getAffiliation()).isPresent();
    }

}
