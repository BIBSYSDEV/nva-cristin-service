package no.unit.nva.model;

import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class ExternalSource {

    @JsonProperty(IDENTIFIER)
    private final transient String identifier;
    @JsonProperty(NAME)
    private final transient String name;

    @JsonCreator
    public ExternalSource(@JsonProperty(IDENTIFIER) String identifier, @JsonProperty(NAME) String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalSource)) {
            return false;
        }
        ExternalSource that = (ExternalSource) o;
        return Objects.equals(getIdentifier(), that.getIdentifier()) && Objects.equals(getName(),
                                                                                       that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentifier(), getName());
    }

}
