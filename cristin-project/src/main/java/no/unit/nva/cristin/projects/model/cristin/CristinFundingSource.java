package no.unit.nva.cristin.projects.model.cristin;

import static java.util.Objects.isNull;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.projects.model.nva.Funding;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinFundingSource implements JsonSerializable {

    public static final String PATH_DELIMITER = "/";

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

    public static CristinFundingSource fromFunding(Funding funding) {
        var cristinFundingSource = new CristinFundingSource();
        cristinFundingSource.setFundingSourceCode(extractFundingSourceCode(funding.getSource()));
        cristinFundingSource.setFundingSourceName(funding.getLabels());
        cristinFundingSource.setProjectCode(funding.getIdentifier());

        return cristinFundingSource;
    }

    /**
     * Extracts funding source code from URI giving a valid Cristin source code.
     */
    public static String extractFundingSourceCode(URI source) {
        if (isNull(source)) {
            return null;
        }
        var sourceAsText = source.toString();
        var lastElementIndexStart = sourceAsText.lastIndexOf(PATH_DELIMITER) + 1;
        var rawSourceCode = sourceAsText.substring(lastElementIndexStart);

        return URLDecoder.decode(rawSourceCode, StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinFundingSource that)) {
            return false;
        }
        return Objects.equals(getFundingSourceCode(), that.getFundingSourceCode()) && Objects.equals(
            getProjectCode(), that.getProjectCode()) && Objects.equals(getFundingSourceName(),
                                                                       that.getFundingSourceName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFundingSourceCode(), getProjectCode(), getFundingSourceName());
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}

