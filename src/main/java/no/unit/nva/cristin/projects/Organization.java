package no.unit.nva.cristin.projects;

import com.google.gson.annotations.SerializedName;

public class Organization {

    public Institution institution;
    @SerializedName("unit")
    public Unit institutionUnit;

}

