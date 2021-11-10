package no.unit.nva.cristin.common.model.cristin;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinInstitution {

    private String cristinInstitutionId;
    private Map<String, String> institutionName;
    private String url;

    public String getCristinInstitutionId() {
        return cristinInstitutionId;
    }

    public void setCristinInstitutionId(String cristinInstitutionId) {
        this.cristinInstitutionId = cristinInstitutionId;
    }

    public Map<String, String> getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(Map<String, String> institutionName) {
        this.institutionName = institutionName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

