package no.unit.nva.cristin.projects;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.TreeMap;

public class Affiliation {

    Institution institution;
    @SerializedName("unit")
    Unit institution_unit;
    Boolean active;
    Map<String, String> position;

    public Institution getInstitution() {
        return institution;
    }

    public Unit getInstitution_unit() {
        return institution_unit;
    }

    public Boolean getActive() {
        return active;
    }

    public Map<String, String> getPosition() {
        if (position == null) {
            position = new TreeMap<>();
        }
        return position;
    }
}

