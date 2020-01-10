package no.unit.nva.cristin.projects;

import com.google.gson.annotations.SerializedName;


@SuppressWarnings("PMD.ShortClassName")
public class Role {

    @SerializedName("role_code")
    public String roleCode;
    public Institution institution;

    @SerializedName("unit")
    public Unit institutionUnit;

}

