package no.unit.nva.cristin.person.model.cristin;

import static no.unit.nva.cristin.model.JsonPropertyNames.AFFILIATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.DISTRIBUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.TITLE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.model.JsonPropertyNames.YEAR;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.model.CristinTypedLabel;

public record CristinAward(@JsonProperty(TITLE) String title,
                           @JsonProperty(YEAR) int year,
                           @JsonProperty(TYPE) CristinTypedLabel type,
                           @JsonProperty(DISTRIBUTION) CristinTypedLabel distribution,
                           @JsonProperty(AFFILIATION) CristinOrganization affiliation) implements JsonSerializable {

    @Override
    public String toString() {
        return toJsonString();
    }

}
