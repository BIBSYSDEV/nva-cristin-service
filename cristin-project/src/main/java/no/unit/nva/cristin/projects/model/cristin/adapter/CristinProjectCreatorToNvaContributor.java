package no.unit.nva.cristin.projects.model.cristin.adapter;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.Person;

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
        Optional.ofNullable(cristinPerson.getRoles())
            .ifPresentOrElse(
                (roles) -> roles.forEach(role -> role.setRoleCode(PRO_CREATOR)),
                () -> cristinPerson.setRoles(emptyList())
            );

        return cristinPerson;
    }

    private Optional<NvaContributor> creatorWithoutAffiliation(CristinPerson creator) {
        return Optional.ofNullable(creator)
                   .filter(presentCreator -> nonNull(presentCreator.getCristinPersonId()))
                   .map(this::extractCreatorIdentity);
    }

    private NvaContributor extractCreatorIdentity(CristinPerson presentCreator) {
        var creatorWithoutAffiliation = new NvaContributor();
        creatorWithoutAffiliation.setIdentity(Person.fromCristinPerson(presentCreator));
        return creatorWithoutAffiliation;
    }

}
