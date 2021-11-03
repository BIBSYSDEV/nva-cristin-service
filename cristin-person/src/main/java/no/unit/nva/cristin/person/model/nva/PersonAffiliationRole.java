package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class PersonAffiliationRole {

    private final URI id;
    private final String type;
    private final Map<String, String> labels;

    /**
     * Creates a PersonAffiliationRole for serialization to client.
     *
     * @param id     URI to ontology.
     * @param type   Type of object, always Role.
     * @param labels Labels in different languages describing this role.
     */
    @JsonCreator
    public PersonAffiliationRole(@JsonProperty("id") URI id, @JsonProperty("type") String type,
                                 @JsonProperty("labels") Map<String, String> labels) {
        this.id = id;
        this.type = type;
        this.labels = labels;
    }

    public URI getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonAffiliationRole)) {
            return false;
        }
        PersonAffiliationRole that = (PersonAffiliationRole) o;
        return getId().equals(that.getId())
            && getType().equals(that.getType())
            && getLabels().equals(that.getLabels());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getId(), getType(), getLabels());
    }
}
