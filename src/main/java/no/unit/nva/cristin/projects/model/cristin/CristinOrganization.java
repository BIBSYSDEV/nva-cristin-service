package no.unit.nva.cristin.projects.model.cristin;

import static no.unit.nva.cristin.projects.JsonPropertyNames.UNIT;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CristinOrganization {

    public CristinInstitution institution;
    @JsonProperty(UNIT)
    public CristinUnit institutionUnit;
}

