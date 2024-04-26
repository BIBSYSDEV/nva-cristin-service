package no.unit.nva.cristin.person.model.cristin.adapter;

import static java.util.Objects.isNull;
import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.person.model.cristin.CristinAward;
import no.unit.nva.cristin.person.model.nva.Award;
import no.unit.nva.model.Organization;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.model.adapter.CristinTypedLabelToNvaFormat;

public class CristinAwardToNvaFormat implements Function<CristinAward, Award> {

    @Override
    public Award apply(CristinAward cristinAward) {
        if (isNull(cristinAward)) {
            return null;
        }

        var name = cristinAward.title();
        var year = cristinAward.year();
        var type = extractTypedLabel(cristinAward.type());
        var distribution = extractTypedLabel(cristinAward.distribution());
        var affiliation = extractAffiliation(cristinAward.affiliation());

        return new Award(name, year, type, distribution, affiliation);
    }

    private static TypedLabel extractTypedLabel(CristinTypedLabel cristinTypedLabel) {
        return Optional.ofNullable(cristinTypedLabel)
                   .map(new CristinTypedLabelToNvaFormat())
                   .orElse(null);
    }

    private static Organization extractAffiliation(CristinOrganization affiliation) {
        return Optional.ofNullable(affiliation)
                   .map(CristinOrganization::extractPreferredTypeOfOrganization)
                   .orElse(null);
    }

}
