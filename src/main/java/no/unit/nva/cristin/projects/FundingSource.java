package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class FundingSource {

    @JsonProperty("funding_source_code")
    public String fundingSourceCode;

    @JsonProperty("project_code")
    public String projectCode;

    @JsonProperty("funding_source_name")
    public Map<String, String> fundingSourceName;

}

