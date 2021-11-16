package no.unit.nva.cristin.projects.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;

import java.net.URI;
import java.util.Objects;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_BASE_URL;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.utils.UriUtils.buildUri;

@SuppressWarnings("unused")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonPropertyOrder({ID, TYPE, FIRST_NAME, LAST_NAME})
public class Person {

    private URI id;
    private String firstName;
    private String lastName;

    @JsonCreator
    public Person(@JsonProperty(ID) URI id,
                  @JsonProperty(FIRST_NAME) String firstName,
                  @JsonProperty(LAST_NAME) String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Build a Person datamodel from a CristinPerson datamodel.
     *
     * @param cristinPerson the model to convert from
     * @return a Person converted from a CristinPerson
     */
    public static Person fromCristinPerson(CristinPerson cristinPerson) {
        if (cristinPerson == null) {
            return null;
        }

        return new Person(buildUri(CRISTIN_API_BASE_URL, PERSON_PATH, cristinPerson.getCristinPersonId()),
                cristinPerson.getFirstName(),
                cristinPerson.getSurname());
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
                && Objects.equals(getLastName(), nvaPerson.getLastName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFirstName(), getLastName());
    }
}
