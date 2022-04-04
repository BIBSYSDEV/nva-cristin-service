package no.unit.nva.cristin.person.affiliations.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import no.unit.nva.utils.UriUtils;
import nva.commons.core.JacocoGenerated;

import java.util.Map;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinPositionCode {

    private String code;
    private Map<String, String> name;
    private boolean enabled;

    public CristinPositionCode() {
    }

    public CristinPositionCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, String> getName() {
        return name;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public PositionCode toPositionCode() {
        return new PositionCode(UriUtils.createNvaPositionId(getCode()), getName(), isEnabled());
    }
}
