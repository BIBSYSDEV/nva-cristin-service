package no.unit.nva.cristin.model.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @Type(CristinSectorFacet.class),
    @Type(CristinInstitutionFacet.class)
})
public abstract class CristinFacet {

    @JsonProperty("count")
    private Integer count;

    public Integer getCount() {
        return count;
    }

    @JsonProperty("key")
    public abstract String getKey();

    @JsonProperty("labels")
    public abstract Map<String, String> getLabels();

}
