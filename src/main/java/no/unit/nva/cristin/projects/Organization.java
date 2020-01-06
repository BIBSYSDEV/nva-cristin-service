package no.unit.nva.cristin.projects;

import com.google.gson.annotations.SerializedName;

public class Organization {

    Institution institution;

    @SerializedName("unit")
    Unit institution_unit;

    public Institution getInstitution() {
        return institution;
    }

    public Unit getInstitution_unit() {
        return institution_unit;
    }
}

