package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Organization {

    public Institution institution;
    @JsonProperty("unit")
    public Unit institutionUnit;

}

