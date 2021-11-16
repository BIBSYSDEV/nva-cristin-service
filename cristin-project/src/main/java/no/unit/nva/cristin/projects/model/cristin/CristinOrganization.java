package no.unit.nva.cristin.projects.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.cristin.model.CristinInstitution;
import nva.commons.core.JacocoGenerated;

import static no.unit.nva.cristin.model.JsonPropertyNames.UNIT;

@SuppressWarnings("unused")
@JacocoGenerated
public class CristinOrganization {

    private CristinInstitution institution;
    @JsonProperty(UNIT)
    private CristinUnit institutionUnit;

    public CristinInstitution getInstitution() {
        return institution;
    }

    public void setInstitution(CristinInstitution institution) {
        this.institution = institution;
    }

    public CristinUnit getInstitutionUnit() {
        return institutionUnit;
    }

    public void setInstitutionUnit(CristinUnit institutionUnit) {
        this.institutionUnit = institutionUnit;
    }
}

