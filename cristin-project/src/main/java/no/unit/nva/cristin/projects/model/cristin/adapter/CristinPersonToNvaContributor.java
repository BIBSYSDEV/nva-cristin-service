package no.unit.nva.cristin.projects.model.cristin.adapter;

import static java.util.Objects.isNull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.model.CristinRole;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.Role;

public class CristinPersonToNvaContributor implements Function<CristinPerson, NvaContributor> {

    @Override
    public NvaContributor apply(CristinPerson cristinPerson) {
        if (isNull(cristinPerson)) {
            return null;
        }

        var identity = new CristinPersonToPerson().apply(cristinPerson);
        var roles = extractRoles(cristinPerson);

        return new NvaContributor(identity, roles);
    }

    private List<Role> extractRoles(CristinPerson cristinPerson) {
        return Optional.ofNullable(cristinPerson.roles())
                   .map(this::cristinRolesToRoles)
                   .orElse(null);
    }

    private List<Role> cristinRolesToRoles(Collection<CristinRole> cristinRoles) {
        return cristinRoles.stream()
                   .map(new CristinRoleToRole())
                   .toList();
    }

}
