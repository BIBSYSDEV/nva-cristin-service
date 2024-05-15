package no.unit.nva.cristin.model;

import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;

public class CristinTypedLabel implements JsonSerializable {

    public static final String CODE = "code";

    @JsonProperty(CODE)
    private final transient String code;
    @JsonProperty(NAME)
    private final transient Map<String, String> name;

    @JsonCreator
    public CristinTypedLabel(@JsonProperty(CODE) String code, @JsonProperty(NAME) Map<String, String> name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public Map<String, String> getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinTypedLabel)) {
            return false;
        }
        CristinTypedLabel that = (CristinTypedLabel) o;
        return Objects.equals(getCode(), that.getCode()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCode(), getName());
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
