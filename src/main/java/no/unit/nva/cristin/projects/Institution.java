package no.unit.nva.cristin.projects;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Institution {

    @SerializedName("cristin_institution_id")
    public String cristinInstitutionId;
    @SerializedName("institution_name")
    public Map<String, String> institutionName;
    public String acronym;
    public String country;
    @SerializedName("cristin_user_institution")
    public Boolean cristinUserInstitution;
    @SerializedName("corresponding_unit")
    public Unit correspondingUnit;
    public String url;

}

