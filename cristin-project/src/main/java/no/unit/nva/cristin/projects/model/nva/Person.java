package no.unit.nva.cristin.projects.model.nva;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Objects;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;
import nva.commons.core.paths.UriWrapper;

@SuppressWarnings("unused")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonPropertyOrder({ID, TYPE, FIRST_NAME, LAST_NAME})
public class Person {

    private final URI id;
    private final String firstName;
    private final String lastName;

    /**
     * Create a valid NVA Person.
     */
    @JsonCreator
    public Person(@JsonProperty(ID) URI id,
                  @JsonProperty(FIRST_NAME) String firstName,
                  @JsonProperty(LAST_NAME) String lastName) {
        this.id = id;
        this.firstName = firstName;
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

        final URI id = new UriWrapper(CRISTIN_API_URL).addChild(PERSON_PATH)
            .addChild(cristinPerson.getCristinPersonId()).getUri();
        return new Person(id,
                          cristinPerson.getFirstName(),
                          cristinPerson.getSurname());
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

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFirstName(), getLastName());
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

    public CristinPerson toCristinPersonWithoutRoles() {
        CristinPerson cristinPerson = new CristinPerson();
        cristinPerson.setCristinPersonId(toCristinPersonIdentity());
        cristinPerson.setUrl(getId().toString());
        cristinPerson.setFirstName(getFirstName());
        cristinPerson.setSurname(getLastName());
        return cristinPerson;
    }

    private String toCristinPersonIdentity() {
        return extractLastPathElement(getId());
    }
}
