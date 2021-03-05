package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.JsonPropertyNames.CORRESPONDING_UNIT;
import static no.unit.nva.cristin.projects.JsonPropertyNames.CRISTIN_INSTITUTION_ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.CRISTIN_USER_INSTITUTION;
import static no.unit.nva.cristin.projects.JsonPropertyNames.INSTITUTION_NAME;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class Institution {

    @JsonProperty(CRISTIN_INSTITUTION_ID)
    public String cristinInstitutionId;
    @JsonProperty(INSTITUTION_NAME)
    public Map<String, String> institutionName;
    public String acronym;
    public String country;
    @JsonProperty(CRISTIN_USER_INSTITUTION)
    public Boolean cristinUserInstitution;
    @JsonProperty(CORRESPONDING_UNIT)
    public Unit correspondingUnit;
    public String url;

}

