package no.unit.nva.cristin.model.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class CristinUnitFacet extends CristinFacet {

    private final String cristinUnitId;
    private final Map<String, String> unitName;

    public CristinUnitFacet(@JsonProperty("cristin_unit_id") String cristinUnitId,
                            @JsonProperty("unit_name") Map<String, String> unitName) {
        super();
        this.cristinUnitId = cristinUnitId;
        this.unitName = unitName;
    }

    @Override
    public String getKey() {
        return cristinUnitId;
    }

    @Override
    public Map<String, String> getLabels() {
        return unitName;
    }

}
