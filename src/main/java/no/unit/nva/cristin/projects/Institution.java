package no.unit.nva.cristin.projects;

import java.util.Map;

public class Institution {

    String cristin_institution_id;
    Map<String, String> institution_name;
    String acronym;
    String country;
    Boolean cristin_user_institution;
    Unit corresponding_unit;
    String url;

    public String getCristin_institution_id() {
        return cristin_institution_id;
    }

    public Map<String, String> getInstitution_name() {
        return institution_name;
    }

    public String getAcronym() {
        return acronym;
    }

    public String getCountry() {
        return country;
    }

    public Boolean getCristin_user_institution() {
        return cristin_user_institution;
    }

    public String getUrl() {
        return url;
    }

    public Unit getCorresponding_unit() {
        return corresponding_unit;
    }
}

