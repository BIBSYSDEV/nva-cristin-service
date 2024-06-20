package no.unit.nva.cristin.projects.model.nva;

import static no.unit.nva.cristin.model.JsonPropertyNames.EMAIL;
import static no.unit.nva.cristin.model.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.PHONE;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Objects;

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
