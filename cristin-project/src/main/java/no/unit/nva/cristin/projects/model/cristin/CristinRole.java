package no.unit.nva.cristin.projects.model.cristin;

import static no.unit.nva.cristin.model.JsonPropertyNames.UNIT;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import no.unit.nva.cristin.model.CristinInstitution;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinRole {

    private String roleCode;
    private CristinInstitution institution;
    @JsonProperty(UNIT)
    private CristinUnit institutionUnit;


    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

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

