package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("PMD.ShortClassName")
public class Role {

    @JsonProperty("role_code")
    public String roleCode;
    public Institution institution;

    @JsonProperty("unit")
    public Unit institutionUnit;

}

