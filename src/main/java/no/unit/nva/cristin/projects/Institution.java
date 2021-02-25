package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class Institution {

    @JsonProperty("cristin_institution_id")
    public String cristinInstitutionId;
    @JsonProperty("institution_name")
    public Map<String, String> institutionName;
    public String acronym;
    public String country;
    @JsonProperty("cristin_user_institution")
    public Boolean cristinUserInstitution;
    @JsonProperty("corresponding_unit")
    public Unit correspondingUnit;
    public String url;

}

