package no.unit.nva.cristin.projects.model.cristin;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinUnit {

    private String cristinUnitId;
    private Map<String, String> unitName;
    private String url;

    public String getCristinUnitId() {
        return cristinUnitId;
    }

    public void setCristinUnitId(String cristinUnitId) {
        this.cristinUnitId = cristinUnitId;
    }

    public Map<String, String> getUnitName() {
        return unitName;
    }

    public void setUnitName(Map<String, String> unitName) {
        this.unitName = unitName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

