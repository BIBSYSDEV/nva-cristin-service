package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class TypedValue implements JsonSerializable {

    private final String type;
    private final String value;

    @JsonCreator
    public TypedValue(@JsonProperty("type") String type, @JsonProperty("value") String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean hasData() {
        return Objects.nonNull(getType()) && Objects.nonNull(getValue());
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypedValue)) {
            return false;
        }
        TypedValue that = (TypedValue) o;
        return Objects.equals(getType(), that.getType()) && Objects.equals(getValue(), that.getValue());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue());
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
