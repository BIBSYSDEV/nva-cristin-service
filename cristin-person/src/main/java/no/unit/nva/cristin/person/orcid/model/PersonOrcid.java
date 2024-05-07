package no.unit.nva.cristin.person.orcid.model;

import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import no.unit.nva.commons.json.JsonSerializable;

public record PersonOrcid(@JsonProperty(ID) URI id,
                          @JsonProperty(ORCID) URI orcid) implements JsonSerializable {

    @Override
    public String toString() {
        return toJsonString();
    }

}
