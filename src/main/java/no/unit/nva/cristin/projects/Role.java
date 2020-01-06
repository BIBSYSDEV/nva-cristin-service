package no.unit.nva.cristin.projects;

import com.google.gson.annotations.SerializedName;

public class Role {

    String role_code;
    Institution institution;

    @SerializedName("unit")
    Unit institution_unit;

    public String getRole_code() {
        return role_code;
    }

    public Institution getInstitution() {
        return institution;
    }

    public Unit getInstitution_unit() {
        return institution_unit;
    }
}

