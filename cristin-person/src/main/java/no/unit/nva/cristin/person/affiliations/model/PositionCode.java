package no.unit.nva.cristin.person.affiliations.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;
import no.unit.nva.cristin.common.Utils;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonSerializable;

@JacocoGenerated
public class PositionCode implements JsonSerializable {

    private final String code;
    private final Map<String, String> name;
    private final boolean enabled;

    /**
     * Creates a PositionCode for serialization to client.
     */
    @JsonCreator
    public PositionCode(@JsonProperty("code") String code, @JsonProperty("name") Map<String, String> name,
                        @JsonProperty("enabled") boolean enabled) {
        this.code = code;
        this.name = name;
        this.enabled = enabled;
    }

    public String getCode() {
        return code;
    }

    public Map<String, String> getName() {
        return Utils.nonEmptyOrDefault(name);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PositionCode)) {
            return false;
        }
        PositionCode that = (PositionCode) o;
        return Objects.equals(getCode(), that.getCode())
            && getName().equals(that.getName())
            && Objects.equals(isEnabled(), that.isEnabled());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getCode(), getName(), isEnabled());
    }
}
