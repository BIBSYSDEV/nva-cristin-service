package no.unit.nva.cristin.person.model.cristin;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.net.URI;
import no.unit.nva.cristin.person.model.nva.PersonInstitutionInfo;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinPersonInstitutionInfo {

    private String email;
    private String phone;

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

    public PersonInstitutionInfo toPersonInstitutionInfo(URI id) {
        return new PersonInstitutionInfo(id, getEmail(), getPhone());
    }
}
