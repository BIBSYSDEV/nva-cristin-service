package no.unit.nva.cristin.model.query;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CristinInstitutionFacet extends CristinFacet {

    private final String id;

    public CristinInstitutionFacet(@JsonProperty("cristin_institution_id") String id) {
        super();
        this.id = id;
    }

    @Override
    public String getKey() {
        return id;
    }

}
