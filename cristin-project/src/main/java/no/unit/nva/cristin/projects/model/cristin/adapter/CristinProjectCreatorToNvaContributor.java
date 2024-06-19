package no.unit.nva.cristin.projects.model.cristin.adapter;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
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
                   .peek(this::addRoleDataIfMissing)
                   .flatMap(this::generateRoleBasedContribution)
                   .findAny()
                   .or(() -> creatorWithoutAffiliation(creator))
                   .orElse(null);
    }

    private void addRoleDataIfMissing(CristinPerson cristinPerson) {
        if (isNull(cristinPerson.getRoles())) {
            cristinPerson.setRoles(emptyList());
        } else {
            cristinPerson.getRoles().forEach(role -> role.setRoleCode(PRO_CREATOR));
        }
    }

    private Optional<NvaContributor> creatorWithoutAffiliation(CristinPerson creator) {
        return Optional.ofNullable(creator)
                   .filter(presentCreator -> nonNull(presentCreator.getCristinPersonId()))
                   .map(presentCreator -> {
                       var creatorWithoutAffiliation = new NvaContributor();
                       creatorWithoutAffiliation.setIdentity(Person.fromCristinPerson(presentCreator));
                       return creatorWithoutAffiliation;
                   });
    }

}
