package no.unit.nva.cristin.person.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.commons.json.JsonSerializable;

public record CristinPersonNvi(@JsonProperty(VERIFIED_BY) CristinPersonSummary verifiedBy) implements JsonSerializable {
    public static final String VERIFIED_BY = "verified_by";
}
