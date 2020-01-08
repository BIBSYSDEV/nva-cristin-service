package no.unit.nva.cristin.projects;

import java.util.Map;

class Institution {

    private String cristin_institution_id;
    private Map<String, String> institution_name;
    private String acronym;
    private String country;
    private Boolean cristin_user_institution;
    private Unit corresponding_unit;
    private String url;

    String getCristin_institution_id() {
        return cristin_institution_id;
    }

    Map<String, String> getInstitution_name() {
        return institution_name;
    }

    String getAcronym() {
        return acronym;
    }

    String getCountry() {
        return country;
    }

    Boolean getCristin_user_institution() {
        return cristin_user_institution;
    }

    String getUrl() {
        return url;
    }

    Unit getCorresponding_unit() {
        return corresponding_unit;
    }
}

