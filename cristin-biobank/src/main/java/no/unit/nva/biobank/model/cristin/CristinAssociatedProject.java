package no.unit.nva.biobank.model.cristin;

import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

import java.net.URI;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CristinAssociatedProject(@JsonProperty("cristin_project_id") String cristinProjectId,
                                       @JsonProperty("title") Map<String, String> title,
                                       @JsonProperty("url") URI url)
    implements JsonSerializable {

    @Override
    public Map<String, String> title() {
        return nonEmptyOrDefault(title);
    }

    @Override
    @JacocoGenerated
    public String toString() {
        return this.toJsonString();
    }
}
