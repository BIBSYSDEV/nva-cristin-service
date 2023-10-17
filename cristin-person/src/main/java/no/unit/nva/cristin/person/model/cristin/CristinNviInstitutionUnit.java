package no.unit.nva.cristin.person.model.cristin;

import static no.unit.nva.cristin.model.JsonPropertyNames.INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.UNIT;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import java.util.Set;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.model.Organization;

public record CristinNviInstitutionUnit(@JsonProperty(INSTITUTION) CristinPersonNviInstitution institution,
                                        @JsonProperty(UNIT) CristinUnit unit) {

    public Organization toOrganization() {
        var organization = Optional.ofNullable(unit).map(CristinUnit::toOrganization);

        if (organization.isPresent()) {
            var present = organization.get();
            var builder = new Organization.Builder();

            builder.withId(present.getId());
            builder.withLabels(present.getLabels());
            builder.withAcronym(present.getAcronym());
            topLevel().ifPresent(top -> builder.withPartOf(convertToSet(top)));
            builder.withCountry(present.getCountry());

            return builder.build();
        } else {
            return null;
        }
    }

    private Set<Organization> convertToSet(Organization organization) {
        return Set.of(organization);
    }

    private Optional<Organization> topLevel() {
        return Optional.ofNullable(institution).map(CristinPersonNviInstitution::toOrganization);
    }
}
