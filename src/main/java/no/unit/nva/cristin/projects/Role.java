package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.JsonPropertyNames.ROLE_CODE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.UNIT;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("PMD.ShortClassName")
public class Role {

    @JsonProperty(ROLE_CODE)
    public String roleCode;
    public Institution institution;

    @JsonProperty(UNIT)
    public Unit institutionUnit;

}

