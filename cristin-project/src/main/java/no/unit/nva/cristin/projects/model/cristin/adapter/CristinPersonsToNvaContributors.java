package no.unit.nva.cristin.projects.model.cristin.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;

public class CristinPersonsToNvaContributors implements Function<Collection<CristinPerson>, List<NvaContributor>> {

    @Override
    public List<NvaContributor> apply(Collection<CristinPerson> cristinPersons) {
        return Optional.ofNullable(cristinPersons)
                   .map(this::convert)
                   .orElse(null);
    }

    private List<NvaContributor> convert(Collection<CristinPerson> cristinPersons) {
        return cristinPersons.stream()
                   .map(new CristinPersonToNvaContributor())
                   .toList();
    }

}
