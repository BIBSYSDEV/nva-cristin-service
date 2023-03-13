package no.unit.nva.biobank.model.cristin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.beans.ConstructorProperties;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinBiobankMaterial {

    private final String materialCode;
    private final Map<String, String> materialName;

    @ConstructorProperties({"materialCode","materialName"})
    public CristinBiobankMaterial(String materialCode, Map<String, String> materialName) {
        this.materialCode = materialCode;
        this.materialName = materialName;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public Map<String, String> getMaterialName() {
        return materialName;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinBiobankMaterial)) {
            return false;
        }
        CristinBiobankMaterial that = (CristinBiobankMaterial) o;
        return Objects.equals(getMaterialCode(), that.getMaterialCode())
               && Objects.equals(getMaterialName(), that.getMaterialName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMaterialCode(), getMaterialName());
    }
}
