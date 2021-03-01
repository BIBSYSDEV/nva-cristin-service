package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.JsonPropertyNames.UNIT;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class Affiliation {

    public Institution institution;
    @JsonProperty(UNIT)
    public Unit institutionUnit;
    public Boolean active;
    public Map<String, String> position;

}

