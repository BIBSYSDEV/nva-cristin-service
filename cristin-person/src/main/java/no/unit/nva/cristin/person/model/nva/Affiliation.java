package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Affiliation {

    private final URI organization;
    private final Boolean active;
    private final Role role;

    /**
     * Creates an Affiliation for serialization to client.
     *
     * @param organization Identifier of Organization.
     * @param active       If this affiliation is currently active.
     * @param role         What roles this Person has at this affiliation.
     */
    @JsonCreator
    public Affiliation(@JsonProperty("organization") URI organization, @JsonProperty("active") Boolean active,
                       @JsonProperty("role") Role role) {
        this.organization = organization;
        this.active = active;
        this.role = role;
    }

    public URI getOrganization() {
        return organization;
    }

    public Boolean getActive() {
        return active;
    }

    public Role getRole() {
        return role;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Affiliation)) {
            return false;
        }
        Affiliation that = (Affiliation) o;
        return getOrganization().equals(that.getOrganization())
            && getActive().equals(that.getActive())
            && getRole().equals(that.getRole());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getOrganization(), getActive(), getRole());
    }

    @JacocoGenerated
    public static final class Builder {

        private transient URI organization;
        private transient Boolean active;
        private transient Role role;

        public Builder withOrganization(URI organization) {
            this.organization = organization;
            return this;
        }

        public Builder withActive(Boolean active) {
            this.active = active;
            return this;
        }

        public Builder withRole(Role role) {
            this.role = role;
            return this;
        }

        public Affiliation build() {
            return new Affiliation(this.organization, this.active, this.role);
        }
    }
}
