package no.unit.nva.cristin.person.model.nva.adapter;

import static java.util.Objects.isNull;
import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.person.model.cristin.CristinAward;
import no.unit.nva.cristin.person.model.nva.Award;
import no.unit.nva.model.Organization;
import no.unit.nva.model.adapter.TypedLabelToCristinFormat;
import no.unit.nva.utils.UriUtils;

public class AwardToCristinFormat implements Function<Award, CristinAward> {

    @Override
    public CristinAward apply(Award award) {
        if (isNull(award)) {
            return null;
        }

        var title = award.name();
        var year = award.year();
        var type = new TypedLabelToCristinFormat().apply(award.type());
        var distribution = new TypedLabelToCristinFormat().apply(award.distribution());
        var affiliation = generateCristinAffiliation(award.affiliation());

        return new CristinAward(title, year, type, distribution, affiliation);
    }

    private CristinOrganization generateCristinAffiliation(Organization affiliation) {
        return Optional.ofNullable(affiliation)
                   .map(Organization::getId)
                   .map(UriUtils::extractLastPathElement)
                   .map(CristinOrganization::fromIdentifier)
                   .orElse(null);
    }

}
