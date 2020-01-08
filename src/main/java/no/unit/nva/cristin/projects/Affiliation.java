package no.unit.nva.cristin.projects;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.TreeMap;

class Affiliation {

    private Institution institution;
    @SerializedName("unit")
    private Unit institution_unit;
    private Boolean active;
    private Map<String, String> position;

    Institution getInstitution() {
        return institution;
    }

    Unit getInstitution_unit() {
        return institution_unit;
    }

    Boolean getActive() {
        return active;
    }

    Map<String, String> getPosition() {
        if (position == null) {
            position = new TreeMap<>();
        }
        return position;
    }
}

