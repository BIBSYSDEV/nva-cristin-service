package no.unit.nva.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.cristin.model.CristinInstitution;
import no.unit.nva.cristin.model.CristinUnit;

public class CristinCoordinatinInstitution {


    public static final String CRISTIN_INSTITUTION = "institution";
    public static final String CRISTIN_UNIT = "unit";

    @JsonProperty(CRISTIN_INSTITUTION)
    private final CristinInstitution cristinInstitution;
    @JsonProperty(CRISTIN_UNIT)
    private final CristinUnit cristinUnit;

    public CristinCoordinatinInstitution(CristinInstitution cristinInstitution, CristinUnit cristinUnit) {
        this.cristinInstitution = cristinInstitution;
        this.cristinUnit = cristinUnit;
    }

    public CristinInstitution getCristinInstitution() {
        return cristinInstitution;
    }

    public CristinUnit getCristinUnit() {
        return cristinUnit;
    }
}
