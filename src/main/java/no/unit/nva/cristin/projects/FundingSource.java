package no.unit.nva.cristin.projects;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class FundingSource {

    @SerializedName("funding_source_code")
    public String fundingSourceCode;

    @SerializedName("project_code")
    public String projectCode;

    @SerializedName("funding_source_name")
    public Map<String, String> fundingSourceName;

}

