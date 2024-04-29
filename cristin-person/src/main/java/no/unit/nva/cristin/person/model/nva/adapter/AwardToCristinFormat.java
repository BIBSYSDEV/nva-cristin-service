package no.unit.nva.cristin.person.model.nva.adapter;

import static java.util.Objects.isNull;
import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.person.model.cristin.CristinAward;
import no.unit.nva.cristin.person.model.cristin.adapter.CristinAwardDistributionMapper;
import no.unit.nva.cristin.person.model.cristin.adapter.CristinAwardTypeMapper;
import no.unit.nva.cristin.person.model.nva.Award;
import no.unit.nva.model.Organization;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.utils.UriUtils;

public class AwardToCristinFormat implements Function<Award, CristinAward> {

    @Override
    public CristinAward apply(Award award) {
        if (isNull(award)) {
            return null;
        }

        var title = award.name();
        var year = award.year();
        var type = generateCristinAwardTypeWithoutLabel(award.type());
        var distribution = generateCristinAwardDistributionWithoutLabel(award.distribution());
        var affiliation = generateCristinAffiliation(award.affiliation());

        return new CristinAward(title, year, type, distribution, affiliation);
    }

    private CristinTypedLabel generateCristinAwardTypeWithoutLabel(TypedLabel awardType) {
        return Optional.ofNullable(awardType)
                   .map(TypedLabel::getType)
                   .flatMap(CristinAwardTypeMapper::getCristinKey)
                   .map(key -> new CristinTypedLabel(key, null))
                   .orElse(null);
    }

    private CristinTypedLabel generateCristinAwardDistributionWithoutLabel(TypedLabel awardDistribution) {
        return Optional.ofNullable(awardDistribution)
                   .map(TypedLabel::getType)
                   .flatMap(CristinAwardDistributionMapper::getCristinKey)
                   .map(key -> new CristinTypedLabel(key, null))
                   .orElse(null);
    }

    private CristinOrganization generateCristinAffiliation(Organization affiliation) {
        return Optional.ofNullable(affiliation)
                   .map(Organization::getId)
                   .map(UriUtils::extractLastPathElement)
                   .map(CristinOrganization::fromIdentifier)
                   .orElse(null);
    }

}
