package no.unit.nva.cristin.model.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class CristinSectorFacet extends CristinFacet {

    private final String code;
    private final Map<String, String> name;

    public CristinSectorFacet(@JsonProperty("code") String code,
                              @JsonProperty("name") Map<String, String> name) {
        super();
        this.code = code;
        this.name = name;
    }

    @Override
    public String getKey() {
        return code;
    }

    @Override
    public Map<String, String> getLabels() {
        return name;
    }

    @Override
    public CristinFacetKey getCristinFacetKey() {
        return CristinFacetKey.SECTOR;
    }
}
