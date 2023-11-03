package no.unit.nva.facet;

import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.LABELS;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.net.URI;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @Type(NvaFacet.class)
})
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
