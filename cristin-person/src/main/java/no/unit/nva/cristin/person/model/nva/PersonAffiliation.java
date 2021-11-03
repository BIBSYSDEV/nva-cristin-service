package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class PersonAffiliation {

    private final String type;
    private final URI organization;
    private final Boolean active;
    private final PersonAffiliationRole role;

    /**
     * Creates a PersonAffiliation for serialization to client.
     *
     * @param type         Type of object, always Affiliation.
     * @param organization Identifier of Organization.
     * @param active       If this affiliation is currently active.
     * @param role         What roles this Person has at this affiliation.
     */
    @JsonCreator
    public PersonAffiliation(@JsonProperty("type") String type, @JsonProperty("organization") URI organization,
                             @JsonProperty("active") Boolean active, @JsonProperty("role") PersonAffiliationRole role) {
        this.type = type;
        this.organization = organization;
        this.active = active;
        this.role = role;
    }

    public String getType() {
        return type;
    }

    public URI getOrganization() {
        return organization;
    }

    public Boolean getActive() {
        return active;
    }

    public PersonAffiliationRole getRole() {
        return role;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonAffiliation)) {
            return false;
        }
        PersonAffiliation that = (PersonAffiliation) o;
        return getType().equals(that.getType())
            && getOrganization().equals(that.getOrganization())
            && getActive().equals(that.getActive())
            && getRole().equals(that.getRole());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getType(), getOrganization(), getActive(), getRole());
    }
}
