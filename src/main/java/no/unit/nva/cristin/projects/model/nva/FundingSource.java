package no.unit.nva.cristin.projects.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Map;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class FundingSource {

    @JsonPropertyOrder(alphabetic = true)
    private final Map<String, String> names;
    private final String code;

    private FundingSource(Builder builder) {
        this.names = builder.names;
        this.code = builder.code;
    }

    @JsonCreator
    public FundingSource(@JsonProperty("names") Map<String, String> names, @JsonProperty("code") String code) {
        this.names = names;
        this.code = code;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "FundingSource{" +
                "names=" + names +
                ", code='" + code + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FundingSource)) return false;
        FundingSource that = (FundingSource) o;
        return Objects.equals(getNames(), that.getNames()) && Objects.equals(getCode(), that.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNames(), getCode());
    }

    public static final class Builder {
        private Map<String, String> names;
        private String code;

        public FundingSource build() {
            return new FundingSource(this);
        }

        public Builder withNames(Map<String, String> names) {
            this.names = names;
            return this;
        }
        public Builder withCode(String code) {
            this.code = code;
            return this;
        }
    }
}
