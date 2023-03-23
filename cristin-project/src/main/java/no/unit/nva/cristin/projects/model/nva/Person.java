package no.unit.nva.cristin.projects.model.nva;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.cristin.model.JsonPropertyNames.EMAIL;
import static no.unit.nva.cristin.model.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.PHONE;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static no.unit.nva.utils.UriUtils.nvaIdentifierToCristinIdentifier;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Objects;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Person {

    private final URI id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;

    /**
     * Create a valid NVA Person.
     */
    @JsonCreator
    public Person(@JsonProperty(ID) URI id,
                  @JsonProperty(FIRST_NAME) String firstName,
                  @JsonProperty(LAST_NAME) String lastName,
                  @JsonProperty(EMAIL)String email,
                  @JsonProperty(PHONE)String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    /**
     * Create a valid NVA Person from Builder.
     */
    public Person(Builder builder) {
        this.id = builder.id;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phone = builder.phone;
    }

    /**
     * Build a Person datamodel from a CristinPerson datamodel.
     *
     * @param cristinPerson the model to convert from
     * @return a Person converted from a CristinPerson
     */
    public static Person fromCristinPerson(CristinPerson cristinPerson) {
        if (isNull(cristinPerson)) {
            return null;
        }

        URI id = getNvaApiId(cristinPerson.getCristinPersonId(), PERSON_PATH_NVA);
        return new Person(
            id,
            cristinPerson.getFirstName(),
            cristinPerson.getSurname(),
            cristinPerson.getEmail(),
            cristinPerson.getPhone());
    }

    public URI getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }


    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFirstName(), getLastName(), getPhone(), getEmail());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Person)) {
            return false;
        }
        Person nvaPerson = (Person) o;
        return getId().equals(nvaPerson.getId())
               && Objects.equals(getFirstName(), nvaPerson.getFirstName())
               && Objects.equals(getLastName(), nvaPerson.getLastName())
               && Objects.equals(getPhone(), nvaPerson.getPhone())
               && Objects.equals(getEmail(), nvaPerson.getEmail());
    }

    /**
     * Create a CristinPerson from this object with all fields but roles.
     *
     * @return CristinPerson converted from Person
     */
    public CristinPerson toCristinPersonWithoutRoles() {
        CristinPerson cristinPerson = new CristinPerson();
        cristinPerson.setCristinPersonId(toCristinPersonIdentity());
        cristinPerson.setUrl(nvaIdentifierToCristinIdentifier(getId(), PERSON_PATH).toString());
        cristinPerson.setFirstName(getFirstName());
        cristinPerson.setSurname(getLastName());
        cristinPerson.setEmail(getEmail());
        cristinPerson.setPhone(getPhone());
        return cristinPerson;
    }

    private String toCristinPersonIdentity() {
        return extractLastPathElement(getId());
    }

    public static final class Builder {

        private transient URI id;
        private transient String firstName;
        private transient String lastName;
        private transient String email;
        private transient String phone;

        public Builder withId(URI id) {
            this.id = id;
            return this;
        }

        public Builder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public Person build() {
            return new Person(this);
        }
    }
}
