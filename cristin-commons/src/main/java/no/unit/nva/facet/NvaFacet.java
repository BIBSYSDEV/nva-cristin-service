package no.unit.nva.facet;

import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.LABELS;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Map;
import no.unit.nva.commons.json.JsonSerializable;

public record NvaFacet(@JsonProperty(ID) URI id,
                       @JsonProperty(COUNT) Integer count,
                       @JsonProperty(KEY) String key,
                       @JsonProperty(NAME) Map<String, String> name,
                       @JsonProperty(LABELS) Map<String, String> labels) implements Facet, JsonSerializable {

    @Override
    public URI getId() {
        return id;
    }

    @Override
    public Integer getCount() {
        return count;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Map<String, String> getNames() {
        return name;
    }

    @Override
    public Map<String, String> getLabels() {
        return labels;
    }

    @Override
    public String toString() {
        return toJsonString();
    }

}
