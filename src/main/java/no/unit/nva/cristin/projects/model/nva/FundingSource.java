package no.unit.nva.cristin.projects.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nva.commons.core.JacocoGenerated;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class FundingSource {

    @JsonPropertyOrder(alphabetic = true)
    private final Map<String, String> names;
    private final String code;

    @JsonCreator
    public FundingSource(@JsonProperty("names") Map<String, String> names, @JsonProperty("code") String code) {
        this.names = names;
        this.code = code;
    }

    public Map<String, String> getNames() {
        return Optional.of(names).orElse(Collections.emptyMap());
    }

    public String getCode() {
        return code;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FundingSource)) {
            return false;
        }
        FundingSource that = (FundingSource) o;
        return Objects.equals(getNames(), that.getNames()) && Objects.equals(getCode(), that.getCode());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getNames(), getCode());
    }
}
