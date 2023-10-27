package no.unit.nva.cristin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CristinInstitutionFacet extends CristinFacet {

    String id;

    public CristinInstitutionFacet(@JsonProperty("cristin_institution_id") String id) {
        super();
        this.id = id;
    }

    @Override
    String getKey() {
        return id;
    }

}
