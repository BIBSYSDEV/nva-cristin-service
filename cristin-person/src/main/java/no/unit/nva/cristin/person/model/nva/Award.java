package no.unit.nva.cristin.person.model.nva;

import static no.unit.nva.cristin.model.JsonPropertyNames.AFFILIATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.DISTRIBUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.model.JsonPropertyNames.YEAR;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.model.Organization;
import no.unit.nva.model.TypedLabel;

public record Award(@JsonProperty(NAME) String name,
                    @JsonProperty(YEAR) int year,
                    @JsonProperty(TYPE) TypedLabel type,
                    @JsonProperty(DISTRIBUTION) TypedLabel distribution,
                    @JsonProperty(AFFILIATION) Organization affiliation) implements JsonSerializable {

    @Override
    public String toString() {
        return toJsonString();
    }

}
