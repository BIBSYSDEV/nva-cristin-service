package no.unit.nva.cristin.projects.model.cristin;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Objects;
import java.util.List;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinPerson {

    private String cristinPersonId;
    private String firstName;
    private String surname;
    private String url;
    private String email;
    private String phone;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<CristinRole> getRoles() {
        return roles;
    }

    public void setRoles(List<CristinRole> roles) {
        this.roles = roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinPerson)) {
            return false;
        }
        CristinPerson that = (CristinPerson) o;
        return Objects.equal(getCristinPersonId(), that.getCristinPersonId())
               && Objects.equal(getFirstName(), that.getFirstName())
               && Objects.equal(getSurname(), that.getSurname())
               && Objects.equal(getUrl(), that.getUrl())
               && Objects.equal(getEmail(), that.getEmail())
               && Objects.equal(getPhone(), that.getPhone())
               && Objects.equal(getRoles(), that.getRoles());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getCristinPersonId(), getFirstName(), getSurname(), getUrl(), getEmail(), getPhone(),
                                getRoles());
    }

}

