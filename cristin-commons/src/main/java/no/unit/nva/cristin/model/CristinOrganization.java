package no.unit.nva.cristin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import java.util.Optional;
import no.unit.nva.model.Organization;
import nva.commons.core.JacocoGenerated;

import static no.unit.nva.cristin.common.Utils.isPositiveInteger;
import static no.unit.nva.cristin.model.CristinUnit.isCristinUnitIdentifier;
import static no.unit.nva.cristin.model.JsonPropertyNames.UNIT;

@SuppressWarnings({"unused", "PMD.DataflowAnomalyAnalysis"})
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

    /**
     * Creates Organization from unit if present which is preferred or else falls back to institution.
     */
    public Organization extractPreferredTypeOfOrganization() {
        var unit = Optional.ofNullable(getInstitutionUnit())
            .map(CristinUnit::toOrganization);
        var institution = Optional.ofNullable(getInstitution())
            .map(CristinInstitution::toOrganization);

        return unit.orElse(institution.orElse(null));
    }

    /**
     * Creates CristinOrganization from identifier. Either Unit which is preferred, or else falls back to institution.
     */
    public static CristinOrganization fromIdentifier(String identifier) {
        var cristinOrganization = new CristinOrganization();
        if (isCristinUnitIdentifier(identifier)) {
            cristinOrganization.setInstitutionUnit(new CristinUnit(identifier));
            return cristinOrganization;
        } else if (isPositiveInteger(identifier)) {
            cristinOrganization.setInstitution(new CristinInstitution(identifier));
            return cristinOrganization;
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (CristinOrganization) o;
        return Objects.equal(institution, that.institution)
               && Objects.equal(institutionUnit, that.institutionUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(institution, institutionUnit);
    }
}

