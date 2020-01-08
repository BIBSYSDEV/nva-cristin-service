package no.unit.nva.cristin.projects;

import com.google.gson.annotations.SerializedName;

class Organization {

    private Institution institution;
    @SerializedName("unit")
    private Unit institution_unit;

    Institution getInstitution() {
        return institution;
    }

    Unit getInstitution_unit() {
        return institution_unit;
    }
}

