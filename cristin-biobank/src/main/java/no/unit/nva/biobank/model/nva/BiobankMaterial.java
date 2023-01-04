package no.unit.nva.biobank.model.nva;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.biobank.model.cristin.CristinBiobankMaterial;

public class BiobankMaterial {
    public static final String MATERIAL_CODE = "materialCode";
    public static final String OTHER_MATERIAL_DESC = "otherMaterialDescription";

    @JsonProperty(MATERIAL_CODE)
    private final String materialCode;
    @JsonProperty(OTHER_MATERIAL_DESC)
    private final String otherMaterialDescription;

    public BiobankMaterial(String materialCode,
                                  String otherMaterialDescription) {
        this.materialCode = materialCode;
        this.otherMaterialDescription = otherMaterialDescription;
    }

    public BiobankMaterial(CristinBiobankMaterial cristinMaterial) {
        this.materialCode = cristinMaterial.getMaterialCode();
        this.otherMaterialDescription = cristinMaterial.getOtherMaterialDescription();
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public String getOtherMaterialDescription() {
        return otherMaterialDescription;
    }
}
