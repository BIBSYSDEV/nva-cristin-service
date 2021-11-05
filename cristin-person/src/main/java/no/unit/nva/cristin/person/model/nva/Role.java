package no.unit.nva.cristin.person.model.nva;

import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import no.unit.nva.cristin.person.model.cristin.CristinAffiliation;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("PMD.ShortClassName")
@JacocoGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Role {

    private final URI id;
    private final Map<String, String> labels;

    /**
     * Creates a Role for serialization to client.
     *
     * @param id     URI to ontology.
     * @param labels Labels in different languages describing this role.
     */
    @JsonCreator
    public Role(@JsonProperty("id") URI id, @JsonProperty("labels") Map<String, String> labels) {
        this.id = id;
        this.labels = labels;
    }

    public URI getId() {
        return id;
    }

    public Map<String, String> getLabels() {
        return labels != null ? labels : Collections.emptyMap();
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
        return Objects.equals(getId(), that.getId()) && getLabels().equals(that.getLabels());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLabels());
    }

    @JacocoGenerated
    public static final class Builder {

        private transient URI id;
        private transient Map<String, String> labels;

        public Builder withId(URI id) {
            this.id = id;
            return this;
        }

        public Builder withLabels(Map<String, String> labels) {
            this.labels = labels;
            return this;
        }

        public Role build() {
            return new Role(this.id, this.labels);
        }
    }

    public static Role fromCristinAffiliation(CristinAffiliation cristinAffiliation) {
        URI uri = attempt(() -> new URI("https://example.org/link/to/ontology#1026")).orElseThrow();
        return new Builder().withId(uri).withLabels(cristinAffiliation.getPosition()).build();
    }
}
