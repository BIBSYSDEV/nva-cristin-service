package no.unit.nva.biobank.model.cristin;

import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CristinBiobankMaterial(@JsonProperty("material_code") String materialCode,
                                     @JsonProperty("material_name") Map<String, String> materialName)
    implements JsonSerializable {

    @Override
    public Map<String, String> materialName() {
        return nonEmptyOrDefault(materialName);
    }

    @Override
    @JacocoGenerated
    public String toString() {
        return toJsonString();
    }

}
