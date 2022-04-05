package no.unit.nva.cristin.person.model.nva;

import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class PersonInstitutionInfo implements JsonSerializable {

    @JsonProperty(CONTEXT)
    private static final String context = "https://ontology.org";
    private final URI id;
    private final String email;
    private final String phone;

    /**
     * Creates a PersonInstitutionInfo for serialization to client.
     *
     * @param id    Identifier for this resource.
     * @param email Person email at this institution.
     * @param phone Person phone at this institution.
     */
    @JsonCreator
    public PersonInstitutionInfo(@JsonProperty("id") URI id, @JsonProperty("email") String email,
                                 @JsonProperty("phone") String phone) {
        this.id = id;
        this.email = email;
        this.phone = phone;
    }

    @JsonProperty(CONTEXT)
    public String getContext() {
        return context;
    }

    public URI getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonInstitutionInfo)) {
            return false;
        }
        PersonInstitutionInfo that = (PersonInstitutionInfo) o;
        return Objects.equals(getContext(), that.getContext())
            && Objects.equals(getId(), that.getId())
            && Objects.equals(getEmail(), that.getEmail())
            && Objects.equals(getPhone(), that.getPhone());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContext(), getId(), getEmail(), getPhone());
    }
}
