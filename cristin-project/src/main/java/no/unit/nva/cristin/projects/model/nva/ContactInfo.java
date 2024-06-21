package no.unit.nva.cristin.projects.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.model.JsonPropertyNames.EMAIL;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.PHONE;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;

@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class ContactInfo implements JsonSerializable {

    public static final String CONTACT_PERSON = "contactPerson";

    @JsonProperty(CONTACT_PERSON)
    private final transient String contactPerson;
    @JsonProperty(ORGANIZATION)
    private final transient String organization;
    @JsonProperty(EMAIL)
    private final transient String email;
    @JsonProperty(PHONE)
    private final transient String phone;

    /**
     * Nva model for a project's contact info.
     */
    @JsonCreator
    public ContactInfo(@JsonProperty(CONTACT_PERSON) String contactPerson,
                       @JsonProperty(ORGANIZATION) String organization,
                       @JsonProperty(EMAIL) String email,
                       @JsonProperty(PHONE) String phone) {
        this.contactPerson = contactPerson;
        this.organization = organization;
        this.email = email;
        this.phone = phone;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getOrganization() {
        return organization;
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
        if (!(o instanceof ContactInfo that)) {
            return false;
        }
        return Objects.equals(getContactPerson(), that.getContactPerson())
               && Objects.equals(getOrganization(), that.getOrganization())
               && Objects.equals(getEmail(), that.getEmail())
               && Objects.equals(getPhone(), that.getPhone());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContactPerson(), getOrganization(), getEmail(), getPhone());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

}
