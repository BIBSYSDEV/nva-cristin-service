package no.unit.nva.cristin.projects.model.cristin;

import static no.unit.nva.cristin.model.JsonPropertyNames.UNIT;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Objects;
import no.unit.nva.cristin.model.CristinInstitution;
import no.unit.nva.cristin.model.CristinUnit;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinRole)) {
            return false;
        }
        CristinRole that = (CristinRole) o;
        return Objects.equals(getRoleCode(), that.getRoleCode())
               && Objects.equals(getInstitution(), that.getInstitution())
               && Objects.equals(getInstitutionUnit(), that.getInstitutionUnit());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRoleCode(), getInstitution(), getInstitutionUnit());
    }

}

