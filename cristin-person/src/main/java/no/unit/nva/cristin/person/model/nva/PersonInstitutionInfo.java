package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonSerializable;

@JacocoGenerated
public class PersonInstitutionInfo implements JsonSerializable {

    private final String email;
    private final String phone;

    /**
     * Creates a PersonInstitutionInfo for serialization to client.
     *
     * @param email Person email at this institution.
     * @param phone Person phone at this institution.
     */
    @JsonCreator
    public PersonInstitutionInfo(@JsonProperty("email") String email, @JsonProperty("phone") String phone) {
        this.email = email;
        this.phone = phone;
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
        return Objects.equals(getEmail(), that.getEmail())
            && Objects.equals(getPhone(), that.getPhone());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmail(), getPhone());
    }
}
