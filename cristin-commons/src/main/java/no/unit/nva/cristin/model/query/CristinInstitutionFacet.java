package no.unit.nva.cristin.model.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class CristinInstitutionFacet extends CristinFacet {

    private final String institutionId;
    private final Map<String, String> institutionName;

    public CristinInstitutionFacet(@JsonProperty("cristin_institution_id") String institutionId,
                                   @JsonProperty("institution_name") Map<String, String> institutionName) {
        super();
        this.institutionId = institutionId;
        this.institutionName = institutionName;
    }

    @Override
    public String getKey() {
        return institutionId;
    }

    @Override
    public Map<String, String> getLabels() {
        return institutionName;
    }

    @Override
    public CristinFacetKey getCristinFacetKey() {
        return CristinFacetKey.INSTITUTION;
    }

}
