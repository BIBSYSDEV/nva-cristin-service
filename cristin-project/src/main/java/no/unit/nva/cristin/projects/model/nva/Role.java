package no.unit.nva.cristin.projects.model.nva;

import static no.unit.nva.cristin.model.JsonPropertyNames.AFFILIATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.model.Organization;

public record Role(@JsonProperty(TYPE) String type,
                   @JsonProperty(AFFILIATION) Organization affiliation) implements JsonSerializable {

    @Override
    public String toString() {
        return toJsonString();
    }

}
