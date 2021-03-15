package no.unit.nva.cristin.projects.model.cristin;

import static no.unit.nva.cristin.projects.JsonPropertyNames.UNIT;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@SuppressWarnings("PMD.ShortClassName")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Role {

    public String roleCode;
    public Institution institution;
    @JsonProperty(UNIT)
    public Unit institutionUnit;

}

