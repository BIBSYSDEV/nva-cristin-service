package no.unit.nva.cristin.model.query;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CristinSectorFacet extends CristinFacet {

    private final String code;

    public CristinSectorFacet(@JsonProperty("code") String code) {
        super();
        this.code = code;
    }

    @Override
    public String getKey() {
        return code;
    }
}
