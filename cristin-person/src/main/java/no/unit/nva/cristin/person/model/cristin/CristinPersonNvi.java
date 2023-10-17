package no.unit.nva.cristin.person.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Optional;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.person.model.nva.PersonNvi;

public record CristinPersonNvi(@JsonProperty(VERIFIED_BY) CristinPersonSummary verifiedBy,
                               @JsonProperty(VERIFIED_AT) CristinNviInstitutionUnit verifiedAt,
                               @JsonProperty(VERIFIED_DATE) Instant verifiedDate)
    implements JsonSerializable {

    public static final String VERIFIED_BY = "verified_by";
    public static final String VERIFIED_AT = "verified_at";
    public static final String VERIFIED_DATE = "verified_date";

    public PersonNvi toPersonNvi() {
        var personSummary = Optional.ofNullable(verifiedBy)
                                .map(CristinPersonSummary::toPersonSummary);

        var organization = Optional.ofNullable(verifiedAt)
                               .map(CristinNviInstitutionUnit::toOrganization);

        if (personSummary.isPresent() || organization.isPresent()) {
            return new PersonNvi(personSummary.orElse(null), organization.orElse(null), verifiedDate);
        }

        return null;
    }

}
