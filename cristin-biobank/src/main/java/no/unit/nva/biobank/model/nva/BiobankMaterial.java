package no.unit.nva.biobank.model.nva;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Map;
import no.unit.nva.biobank.model.cristin.CristinBiobankMaterial;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

public class BiobankMaterial implements JsonSerializable {
    @JsonProperty
    private String code;
    @JsonProperty
    @JsonPropertyOrder(alphabetic = true)
    private Map<String, String> name;

    @JacocoGenerated
    public BiobankMaterial() {}

    public BiobankMaterial(CristinBiobankMaterial cristinMaterial) {
        this.code = cristinMaterial.getMaterialCode();
        this.name = cristinMaterial.getMaterialName();
    }

    public String getCode() {
        return code;
    }

    public Map<String, String> getName() {
        return name;
    }
}
