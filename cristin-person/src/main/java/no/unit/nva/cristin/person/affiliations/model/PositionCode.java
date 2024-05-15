package no.unit.nva.cristin.person.affiliations.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.common.Utils;
import nva.commons.core.JacocoGenerated;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

@JacocoGenerated
public class PositionCode implements JsonSerializable {

    private final URI id;
    private final Map<String, String> labels;
    private final boolean enabled;

    /**
     * Creates a PositionCode for serialization to client.
     */
    @JsonCreator
    public PositionCode(@JsonProperty("id") URI id, @JsonProperty("labels") Map<String, String> labels,
                        @JsonProperty("enabled") boolean enabled) {
        this.id = id;
        this.labels = labels;
        this.enabled = enabled;
    }

    public URI getId() {
        return id;
    }

    public Map<String, String> getLabels() {
        return Utils.nonEmptyOrDefault(labels);
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
        return Objects.equals(getId(), that.getId())
               && getLabels().equals(that.getLabels())
               && Objects.equals(isEnabled(), that.isEnabled());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLabels(), isEnabled());
    }
}
