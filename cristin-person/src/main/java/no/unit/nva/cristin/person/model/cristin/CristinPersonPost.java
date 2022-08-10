package no.unit.nva.cristin.person.model.cristin;

import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.AFFILIATIONS;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinPersonPost extends CristinPerson implements JsonSerializable {

    private List<CristinPersonEmployment> detailedAffiliations;

    @JsonProperty(AFFILIATIONS)
    public List<CristinPersonEmployment> getDetailedAffiliations() {
        return detailedAffiliations;
    }

    @JsonProperty(AFFILIATIONS)
    public void setDetailedAffiliations(List<CristinPersonEmployment> detailedAffiliations) {
        this.detailedAffiliations = detailedAffiliations;
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
