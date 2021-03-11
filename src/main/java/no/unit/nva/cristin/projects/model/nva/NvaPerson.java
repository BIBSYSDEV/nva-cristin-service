package no.unit.nva.cristin.projects.model.nva;

import static no.unit.nva.cristin.projects.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.projects.JsonPropertyNames.ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.projects.JsonPropertyNames.TYPE;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
@JsonPropertyOrder({ID, TYPE, FIRST_NAME, LAST_NAME})
public class NvaPerson {

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
}
