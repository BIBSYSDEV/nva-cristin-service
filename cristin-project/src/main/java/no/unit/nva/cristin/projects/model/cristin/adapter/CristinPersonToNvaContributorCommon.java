package no.unit.nva.cristin.projects.model.cristin.adapter;

import static no.unit.nva.cristin.projects.model.nva.ContributorRoleMapping.getNvaRole;
import java.util.Optional;
import java.util.stream.Stream;
import no.unit.nva.cristin.model.CristinInstitution;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.model.CristinRole;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.model.Organization;

public class CristinPersonToNvaContributorCommon {

    protected CristinPersonToNvaContributorCommon() {
    }

    protected Stream<NvaContributor> generateRoleBasedContribution(CristinPerson cristinPerson) {
        return cristinPerson.roles()
                   .stream()
                   .map(role -> createNvaContributorFromCristinPersonByRole(cristinPerson, role));
    }

    private NvaContributor createNvaContributorFromCristinPersonByRole(CristinPerson cristinPerson,
                                                                       CristinRole role) {
        var nvaContributor = new NvaContributor();
        if (getNvaRole(role.getRoleCode()).isPresent()) {
            nvaContributor.setType(getNvaRole(role.getRoleCode()).get());
        }
        nvaContributor.setIdentity(new CristinPersonToPerson().apply(cristinPerson));
        nvaContributor.setAffiliation(extractDepartmentOrFallbackToInstitutionForUserRole(role));
        return nvaContributor;
    }

    private Organization extractDepartmentOrFallbackToInstitutionForUserRole(CristinRole role) {
        var unitAffiliation = Optional.ofNullable(role.getInstitutionUnit())
                                  .map(CristinUnit::toOrganization);
        var institutionAffiliation = Optional.ofNullable(role.getInstitution())
                                         .map(CristinInstitution::toOrganization);

        return unitAffiliation.orElse(institutionAffiliation.orElse(null));
    }

}
