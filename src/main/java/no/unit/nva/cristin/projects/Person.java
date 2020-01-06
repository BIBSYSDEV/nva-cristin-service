package no.unit.nva.cristin.projects;

import java.util.List;

public class Person {

    String cristin_person_id;
    String first_name;
    String surname;
    String tel;
    Boolean identified_cristin_person;
    String cristin_profile_url;
    String picture_url;
    String url;
    List<Role> roles;
    List<Affiliation> affiliations;

    public String getCristin_person_id() {
        return cristin_person_id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getSurname() {
        return surname;
    }

    public String getUrl() {
        return url;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public String getTel() {
        return tel;
    }

    public Boolean getIdentified_cristin_person() {
        return identified_cristin_person;
    }

    public String getCristin_profile_url() {
        return cristin_profile_url;
    }

    public String getPicture_url() {
        return picture_url;
    }

    public List<Affiliation> getAffiliations() {
        return affiliations;
    }
}

