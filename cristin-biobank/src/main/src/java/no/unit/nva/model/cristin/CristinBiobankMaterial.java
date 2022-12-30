package no.unit.nva.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CristinBiobankMaterial {

    public static final String CRISTIN_MATERIAL_CODE = "material_code";
    public static final String CRISTIN_OTHER_MATERIAL_DESC = "other_material_description";

    @JsonProperty(CRISTIN_MATERIAL_CODE)
    private final String materialCode;
    @JsonProperty(CRISTIN_OTHER_MATERIAL_DESC)
    private final String otherMaterialDescription;

    public CristinBiobankMaterial(String materialCode,
                                  String otherMaterialDescription) {
        this.materialCode = materialCode;
        this.otherMaterialDescription = otherMaterialDescription;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public String getOtherMaterialDescription() {
        return otherMaterialDescription;
    }
}
