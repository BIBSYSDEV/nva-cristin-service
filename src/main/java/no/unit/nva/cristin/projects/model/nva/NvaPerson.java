package no.unit.nva.cristin.projects.model.nva;

import java.net.URI;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
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
