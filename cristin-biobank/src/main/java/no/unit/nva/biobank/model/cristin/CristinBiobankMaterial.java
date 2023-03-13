package no.unit.nva.biobank.model.cristin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinBiobankMaterial {

    @JsonProperty
    private String materialCode;
    @JsonProperty
    private Map<String, String> materialName;

    public CristinBiobankMaterial() {
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
