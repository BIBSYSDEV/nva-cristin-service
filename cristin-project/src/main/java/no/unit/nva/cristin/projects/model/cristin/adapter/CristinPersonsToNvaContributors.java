package no.unit.nva.cristin.projects.model.cristin.adapter;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;

public class CristinPersonsToNvaContributors extends CristinPersonToNvaContributorCommon
    implements Function<List<CristinPerson>, List<NvaContributor>> {

    @Override
    public List<NvaContributor> apply(List<CristinPerson> cristinPersons) {
        return cristinPersons.stream()
                   .flatMap(this::generateRoleBasedContribution)
                   .collect(Collectors.toList());
    }

}
