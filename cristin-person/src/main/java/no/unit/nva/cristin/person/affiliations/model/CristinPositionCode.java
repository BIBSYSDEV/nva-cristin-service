package no.unit.nva.cristin.person.affiliations.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Objects;
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
        return new PositionCode(UriUtils.createNvaPositionId(getCode()), getName(), getName(), isEnabled());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinPositionCode)) {
            return false;
        }
        CristinPositionCode that = (CristinPositionCode) o;
        return isEnabled() == that.isEnabled()
               && Objects.equals(getCode(), that.getCode())
               && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCode(), getName(), isEnabled());
    }
}
