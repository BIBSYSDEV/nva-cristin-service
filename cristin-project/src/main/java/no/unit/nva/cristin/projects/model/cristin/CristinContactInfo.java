package no.unit.nva.cristin.projects.model.cristin;

import static no.unit.nva.cristin.model.JsonPropertyNames.EMAIL;
import static no.unit.nva.cristin.model.JsonPropertyNames.INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.PHONE;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Objects;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinContactInfo {

    public static final String CRISTIN_CONTACT_PERSON = "contact_person";

    private final transient String contactPerson;
    private final transient String institution;
    private final transient String email;
    private final transient String phone;

    /**
     * Cristin model for a project's contact info.
     */
    @JsonCreator
    public CristinContactInfo(@JsonProperty(CRISTIN_CONTACT_PERSON) String contactPerson,
                              @JsonProperty(INSTITUTION) String institution,
                              @JsonProperty(EMAIL) String email,
                              @JsonProperty(PHONE) String phone) {
        this.contactPerson = contactPerson;
        this.institution = institution;
        this.email = email;
        this.phone = phone;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getInstitution() {
        return institution;
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
        if (!(o instanceof CristinContactInfo)) {
            return false;
        }
        CristinContactInfo that = (CristinContactInfo) o;
        return Objects.equals(getContactPerson(), that.getContactPerson())
               && Objects.equals(getInstitution(), that.getInstitution())
               && Objects.equals(getEmail(), that.getEmail())
               && Objects.equals(getPhone(), that.getPhone());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContactPerson(), getInstitution(), getEmail(), getPhone());
    }
}
