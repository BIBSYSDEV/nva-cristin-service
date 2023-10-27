package no.unit.nva.cristin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CristinSectorFacet extends CristinFacet {

    String code;

    public CristinSectorFacet(@JsonProperty("code") String code) {
        super();
        this.code = code;
    }

    @Override
    String getKey() {
        return code;
    }
}
