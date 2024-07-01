package no.unit.nva.cristin.projects.model.cristin.adapter;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.model.CristinInstitution.fromOrganization;
import static no.unit.nva.cristin.model.CristinUnit.extractUnitIdentifier;
import static no.unit.nva.cristin.projects.model.nva.ContributorRoleMapping.getCristinRole;
import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinRole;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.projects.model.nva.Role;
import no.unit.nva.model.Organization;

public class RoleToCristinRole implements Function<Role, CristinRole> {

    @Override
    public CristinRole apply(Role role) {
        if (isNull(role)) {
            return null;
        }

        var cristinRole = new CristinRole();

        cristinRole.setRoleCode(extractRoleCode(role.type()));

        extractInstitutionUnit(role.affiliation()).ifPresentOrElse(
            cristinRole::setInstitutionUnit,
            () -> cristinRole.setInstitution(fromOrganization(role.affiliation()))
        );

        return cristinRole;
    }

    private String extractRoleCode(String type) {
        return getCristinRole(type).orElse(null);
    }

    private Optional<CristinUnit> extractInstitutionUnit(Organization organization) {
        return extractUnitIdentifier(organization)
                   .map(CristinUnit::new);
    }

}
