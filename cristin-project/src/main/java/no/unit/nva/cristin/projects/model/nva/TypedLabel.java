package no.unit.nva.cristin.projects.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;

@JsonInclude(NON_NULL)
public class TypedLabel implements JsonSerializable {

    public static final String LABELS = "labels";

    @JsonProperty(TYPE)
    private final transient String type;
    @JsonProperty(LABELS)
    private final transient Map<String, String> labels;

    public TypedLabel(@JsonProperty(TYPE) String type, @JsonProperty(LABELS) Map<String, String> labels) {
        this.type = type;
        this.labels = labels;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getLabels() {
        return nonEmptyOrDefault(labels);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypedLabel)) {
            return false;
        }
        TypedLabel that = (TypedLabel) o;
        return Objects.equals(getType(), that.getType()) && Objects.equals(getLabels(),
                                                                           that.getLabels());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getLabels());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

}
