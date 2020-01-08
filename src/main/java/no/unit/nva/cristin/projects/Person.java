package no.unit.nva.cristin.projects;

import java.util.List;

class Person {

    private String cristin_person_id;
    private String first_name;
    private String surname;
    private String tel;
    private Boolean identified_cristin_person;
    private String cristin_profile_url;
    private String picture_url;
    private String url;
    private List<Role> roles;
    private List<Affiliation> affiliations;

    String getCristin_person_id() {
        return cristin_person_id;
    }

    String getFirst_name() {
        return first_name;
    }

    String getSurname() {
        return surname;
    }

    String getUrl() {
        return url;
    }

    List<Role> getRoles() {
        return roles;
    }

    String getTel() {
        return tel;
    }

    Boolean getIdentified_cristin_person() {
        return identified_cristin_person;
    }

    String getCristin_profile_url() {
        return cristin_profile_url;
    }

    String getPicture_url() {
        return picture_url;
    }

    List<Affiliation> getAffiliations() {
        return affiliations;
    }
}

