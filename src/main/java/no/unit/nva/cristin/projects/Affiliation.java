package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class Affiliation {

    public Institution institution;
    @JsonProperty("unit")
    public Unit institutionUnit;
    public Boolean active;
    public Map<String, String> position;

}

