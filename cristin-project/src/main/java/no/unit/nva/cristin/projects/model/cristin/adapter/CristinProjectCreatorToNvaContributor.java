package no.unit.nva.cristin.projects.model.cristin.adapter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.model.CristinRole;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;

public class CristinProjectCreatorToNvaContributor implements Function<CristinPerson, NvaContributor> {

    public static final String PRO_CREATOR = "PRO_CREATOR";

    @Override
    public NvaContributor apply(CristinPerson cristinPerson) {
        return extractCreator(cristinPerson);
    }

    private NvaContributor extractCreator(CristinPerson creator) {
        return Optional.ofNullable(creator)
                   .stream()
                   .map(this::addRoleDataIfMissing)
                   .map(new CristinPersonToNvaContributor())
                   .findAny()
                   .or(() -> creatorWithoutRoles(creator))
                   .orElse(null);
    }

    private CristinPerson addRoleDataIfMissing(CristinPerson cristinPerson) {
        var roles = Optional.ofNullable(cristinPerson.roles())
                        .map(this::addProjectCreatorRoleCode)
                        .orElse(Collections.emptyList());

        return cristinPerson.copy()
                   .withRoles(roles)
                   .build();
    }

    private List<CristinRole> addProjectCreatorRoleCode(List<CristinRole> cristinRoles) {
        return cristinRoles.stream()
                   .peek(role -> role.setRoleCode(PRO_CREATOR))
                   .toList();
    }

    private Optional<NvaContributor> creatorWithoutRoles(CristinPerson creator) {
        return Optional.ofNullable(creator)
                   .map(new CristinPersonToPerson())
                   .map(identity -> new NvaContributor(identity, null));
    }

}
