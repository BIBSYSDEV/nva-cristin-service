package no.unit.nva.cristin.projects.model.cristin.adapter;

import static java.util.Objects.isNull;
import java.util.List;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.model.CristinRole;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;

public class NvaContributorToCristinPerson implements Function<NvaContributor, CristinPerson> {

    @Override
    public CristinPerson apply(NvaContributor nvaContributor) {
        if (isNull(nvaContributor)) {
            return null;
        }

        return toCristinPersonWithRoles(nvaContributor);
    }

    /**
     * Create a CristinPerson from identity with added roles.
     *
     * @return a CristinPerson from identity and roles
     */
    public CristinPerson toCristinPersonWithRoles(NvaContributor nvaContributor) {
        var cristinPerson = new PersonToCristinPerson().apply(nvaContributor.identity());

        return cristinPerson.copy()
                   .withRoles(extractCristinRoles(nvaContributor))
                   .build();
    }

    private List<CristinRole> extractCristinRoles(NvaContributor nvaContributor) {
        return nvaContributor.roles().stream()
                   .map(new RoleToCristinRole())
                   .toList();
    }

}
