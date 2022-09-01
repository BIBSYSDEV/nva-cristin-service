package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("PMD.ShortClassName")
@JacocoGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Role {

    private final Map<String, String> labels;

    /**
     * Creates a Role for serialization to client.
     *
     * @param labels Labels in different languages describing this role.
     */
    @JsonCreator
    public Role(@JsonProperty("labels") Map<String, String> labels) {
        this.labels = labels;
    }


    public Map<String, String> getLabels() {
        return Objects.nonNull(labels) ? labels : Collections.emptyMap();
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Role)) {
            return false;
        }
        Role that = (Role) o;
        return Objects.equals(getLabels(), that.getLabels());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getLabels());
    }

}
