package no.unit.nva.cristin.projects.model.cristin;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinFundingSource {

    private String fundingSourceCode;
    private String projectCode;
    private Map<String, String> fundingSourceName;

    public String getFundingSourceCode() {
        return fundingSourceCode;
    }

    public void setFundingSourceCode(String fundingSourceCode) {
        this.fundingSourceCode = fundingSourceCode;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public Map<String, String> getFundingSourceName() {
        return fundingSourceName;
    }

    public void setFundingSourceName(Map<String, String> fundingSourceName) {
        this.fundingSourceName = fundingSourceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinFundingSource)) {
            return false;
        }
        CristinFundingSource that = (CristinFundingSource) o;
        return Objects.equals(getFundingSourceCode(), that.getFundingSourceCode()) && Objects.equals(
            getProjectCode(), that.getProjectCode()) && Objects.equals(getFundingSourceName(),
                                                                       that.getFundingSourceName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFundingSourceCode(), getProjectCode(), getFundingSourceName());
    }

}

