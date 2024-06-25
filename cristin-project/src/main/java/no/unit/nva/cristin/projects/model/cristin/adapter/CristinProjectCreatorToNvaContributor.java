package no.unit.nva.cristin.projects.model.cristin.adapter;

import static java.util.Objects.nonNull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.model.CristinRole;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;

public class CristinProjectCreatorToNvaContributor extends CristinPersonToNvaContributorCommon
    implements Function<CristinPerson, NvaContributor> {

    public static final String PRO_CREATOR = "PRO_CREATOR";

    @Override
    public NvaContributor apply(CristinPerson cristinPerson) {
        return extractCreator(cristinPerson);
    }

    private NvaContributor extractCreator(CristinPerson creator) {
        return Optional.ofNullable(creator)
                   .stream()
                   .map(this::addRoleDataIfMissing)
                   .flatMap(this::generateRoleBasedContribution)
                   .findAny()
                   .or(() -> creatorWithoutAffiliation(creator))
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

    private Optional<NvaContributor> creatorWithoutAffiliation(CristinPerson creator) {
        return Optional.ofNullable(creator)
                   .filter(presentCreator -> nonNull(presentCreator.cristinPersonId()))
                   .map(this::extractCreatorIdentity);
    }

    private NvaContributor extractCreatorIdentity(CristinPerson presentCreator) {
        var creatorWithoutAffiliation = new NvaContributor();
        creatorWithoutAffiliation.setIdentity(new CristinPersonToPerson().apply(presentCreator));
        return creatorWithoutAffiliation;
    }

}
