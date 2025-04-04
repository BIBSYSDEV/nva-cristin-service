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

public class CristinAwardToNvaFormat implements Function<CristinAward, Award> {

    @Override
    public Award apply(CristinAward cristinAward) {
        if (isNull(cristinAward)) {
            return null;
        }

        var name = cristinAward.title();
        var year = cristinAward.year();
        var awardFor = generateAwardTypeWithLabel(cristinAward.type());
        var distribution = generateAwardDistributionWithLabel(cristinAward.distribution());
        var affiliation = extractAffiliation(cristinAward.affiliation());

        return new Award(name, year, awardFor, distribution, affiliation);
    }

    private TypedLabel generateAwardTypeWithLabel(CristinTypedLabel cristinAwardType) {
        return Optional.ofNullable(cristinAwardType)
                   .map(CristinTypedLabel::getCode)
                   .flatMap(CristinAwardTypeMapper::getNvaKey)
                   .map(type -> new TypedLabel(type, cristinAwardType.getName()))
                   .orElse(null);
    }

    private TypedLabel generateAwardDistributionWithLabel(CristinTypedLabel cristinAwardDistribution) {
        return Optional.ofNullable(cristinAwardDistribution)
                   .map(CristinTypedLabel::getCode)
                   .flatMap(CristinAwardDistributionMapper::getNvaKey)
                   .map(type -> new TypedLabel(type, cristinAwardDistribution.getName()))
                   .orElse(null);
    }

    private static Organization extractAffiliation(CristinOrganization affiliation) {
        return Optional.ofNullable(affiliation)
                   .map(CristinOrganization::extractPreferredTypeOfOrganization)
                   .orElse(null);
    }

}
