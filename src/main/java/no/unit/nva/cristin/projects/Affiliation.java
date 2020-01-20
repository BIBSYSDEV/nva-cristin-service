package no.unit.nva.cristin.projects;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Affiliation {

    public Institution institution;
    @SerializedName("unit")
    public Unit institutionUnit;
    public Boolean active;
    public Map<String, String> position;

}

