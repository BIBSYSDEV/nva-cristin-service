package no.unit.nva.cristin.projects;

import com.google.gson.annotations.SerializedName;

class Role {

    private String role_code;
    private Institution institution;

    @SerializedName("unit")
    private Unit institution_unit;

    String getRole_code() {
        return role_code;
    }

    Institution getInstitution() {
        return institution;
    }

    Unit getInstitution_unit() {
        return institution_unit;
    }
}

