package no.unit.nva.cristin.projects.model.nva;

import static no.unit.nva.cristin.projects.Constants.CRISTIN_API_BASE_URL;
import static no.unit.nva.cristin.projects.Constants.PERSON_PATH;
import static no.unit.nva.cristin.projects.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.projects.JsonPropertyNames.ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.projects.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.projects.UriUtils.buildUri;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.util.Objects;

import no.unit.nva.cristin.projects.model.cristin.CristinPerson;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonPropertyOrder({ID, TYPE, FIRST_NAME, LAST_NAME})
public class NvaPerson {

    @JsonIgnore
    private static final String PERSON_TYPE = "Person";

    private URI id;
    private String type;
    private String firstName;
    private String lastName;

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
     * Build a NvaPerson datamodel from a CristinPerson datamodel.
     *
     * @param cristinPerson the model to convert from
     * @return a NvaPerson converted from a CristinPerson
     */
    public static NvaPerson fromCristinPerson(CristinPerson cristinPerson) {
        if (cristinPerson == null) {
            return null;
        }

        NvaPerson identity = new NvaPerson();
        identity.setId(buildUri(CRISTIN_API_BASE_URL, PERSON_PATH, cristinPerson.getCristinPersonId()));
        identity.setType(PERSON_TYPE);
        identity.setFirstName(cristinPerson.getFirstName());
        identity.setLastName(cristinPerson.getSurname());
        return identity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NvaPerson)) {
            return false;
        }
        NvaPerson nvaPerson = (NvaPerson) o;
        return getId().equals(nvaPerson.getId())
                && getType().equals(nvaPerson.getType())
                && Objects.equals(getFirstName(), nvaPerson.getFirstName())
                && Objects.equals(getLastName(), nvaPerson.getLastName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getType(), getFirstName(), getLastName());
    }
}
