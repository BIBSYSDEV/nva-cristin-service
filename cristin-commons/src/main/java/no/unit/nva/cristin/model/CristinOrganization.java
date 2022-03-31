package no.unit.nva.cristin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import no.unit.nva.model.Organization;
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

    /**
     * Creates Organization from unit if present which is preferred or else falls back to institution.
     */
    public Organization extractPreferredTypeOfOrganization() {
        Optional<Organization> unit = Optional.ofNullable(getInstitutionUnit())
            .map(CristinUnit::toOrganization);
        Optional<Organization> institution = Optional.ofNullable(getInstitution())
            .map(CristinInstitution::toOrganization);

        return unit.orElse(institution.orElse(null));
    }
}

