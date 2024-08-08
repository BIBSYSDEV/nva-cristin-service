package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public record TypedValue(@JsonProperty("type") String type,
                         @JsonProperty("value") String value) implements JsonSerializable {

    public boolean hasData() {
        return Objects.nonNull(type()) && Objects.nonNull(value());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

}
