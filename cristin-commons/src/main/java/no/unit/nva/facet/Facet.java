package no.unit.nva.facet;

import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.LABELS;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Map;

public interface Facet {

    String COUNT = "count";
    String KEY = "key";

    @JsonProperty(ID)
    URI getId();

    @JsonProperty(COUNT)
    Integer getCount();

    @JsonProperty(KEY)
    String getKey();

    @JsonProperty(NAME)
    Map<String, String> getNames();

    @JsonProperty(LABELS)
    Map<String, String> getLabels();

}
