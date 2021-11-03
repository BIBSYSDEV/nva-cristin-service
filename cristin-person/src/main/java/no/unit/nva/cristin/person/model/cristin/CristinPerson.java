package no.unit.nva.cristin.person.model.cristin;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinPerson {

    private String cristinPersonId;
    private CristinOrcid orcid;
    private String firstName;
    private String surname;
    private String firstNamePreferred;
    private String surnamePreferred;
    private String tel;
    private String pictureUrl;
    private List<CristinAffiliation> affiliations;

    public String getCristinPersonId() {
        return cristinPersonId;
    }

    public void setCristinPersonId(String cristinPersonId) {
        this.cristinPersonId = cristinPersonId;
    }

    public CristinOrcid getOrcid() {
        return orcid;
    }

    public void setOrcid(CristinOrcid orcid) {
        this.orcid = orcid;
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

    public String getFirstNamePreferred() {
        return firstNamePreferred;
    }

    public void setFirstNamePreferred(String firstNamePreferred) {
        this.firstNamePreferred = firstNamePreferred;
    }

    public String getSurnamePreferred() {
        return surnamePreferred;
    }

    public void setSurnamePreferred(String surnamePreferred) {
        this.surnamePreferred = surnamePreferred;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public List<CristinAffiliation> getAffiliations() {
        return affiliations;
    }

    public void setAffiliations(List<CristinAffiliation> affiliations) {
        this.affiliations = affiliations;
    }
}
