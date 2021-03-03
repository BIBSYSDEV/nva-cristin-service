package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.JsonPropertyNames.UNIT;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Organization {

    public Institution institution;
    @JsonProperty(UNIT)
    public Unit institutionUnit;

}

