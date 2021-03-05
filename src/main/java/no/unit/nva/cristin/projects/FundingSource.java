package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.JsonPropertyNames.FUNDING_SOURCE_CODE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.FUNDING_SOURCE_NAME;
import static no.unit.nva.cristin.projects.JsonPropertyNames.PROJECT_CODE;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class FundingSource {

    @JsonProperty(FUNDING_SOURCE_CODE)
    public String fundingSourceCode;

    @JsonProperty(PROJECT_CODE)
    public String projectCode;

    @JsonProperty(FUNDING_SOURCE_NAME)
    public Map<String, String> fundingSourceName;

}

