package no.unit.nva.cristin.projects.model.cristin;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinPerson {

    private String cristinPersonId;
    private String firstName;
    private String surname;
    private String url;
    private List<CristinRole> roles;

    public String getCristinPersonId() {
        return cristinPersonId;
    }

    public void setCristinPersonId(String cristinPersonId) {
        this.cristinPersonId = cristinPersonId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<CristinRole> getRoles() {
        return roles;
    }

    public void setRoles(List<CristinRole> roles) {
        this.roles = roles;
    }
}

