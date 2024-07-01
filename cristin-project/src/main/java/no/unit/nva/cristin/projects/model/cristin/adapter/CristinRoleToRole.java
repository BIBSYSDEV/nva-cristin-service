package no.unit.nva.cristin.projects.model.cristin.adapter;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.projects.model.nva.ContributorRoleMapping.getNvaRole;
import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinInstitution;
import no.unit.nva.cristin.model.CristinRole;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.projects.model.nva.Role;
import no.unit.nva.model.Organization;

public class CristinRoleToRole implements Function<CristinRole, Role> {

    @Override
    public Role apply(CristinRole cristinRole) {
        if (isNull(cristinRole)) {
            return null;
        }

        var role = extractNvaRole(cristinRole);
        var organization = extractPreferredOrganization(cristinRole);

        return new Role(role, organization);
    }

    private String extractNvaRole(CristinRole cristinRole) {
        return getNvaRole(cristinRole.getRoleCode())
                   .orElse(null);
    }

    private Organization extractPreferredOrganization(CristinRole role) {
        return extractUnit(role)
                   .or(() -> extractInstitution(role))
                   .orElse(null);
    }

    private Optional<Organization> extractUnit(CristinRole role) {
        return Optional.ofNullable(role.getInstitutionUnit())
                   .map(CristinUnit::toOrganization);
    }

    private Optional<Organization> extractInstitution(CristinRole role) {
        return Optional.ofNullable(role.getInstitution())
                   .map(CristinInstitution::toOrganization);
    }

}
