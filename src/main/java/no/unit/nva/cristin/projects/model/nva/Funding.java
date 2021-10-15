package no.unit.nva.cristin.projects.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Funding {

    private final FundingSource source;
    private final String code;

    private Funding(Builder builder) {
        this.source = builder.source;
        this.code = builder.code;
    }

    @JsonCreator
    public Funding(@JsonProperty("source") FundingSource source, @JsonProperty("code") String code) {
        this.source = source;
        this.code = code;
    }

    public FundingSource getSource() {
        return source;
    }

    public String getCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Funding)) return false;
        Funding that = (Funding) o;
        return getSource().equals(that.getSource()) && Objects.equals(getCode(), that.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSource(), getCode());
    }

    @Override
    public String toString() {
        return "Funding{" +
                "source=" + source +
                ", code='" + code + '\'' +
                '}';
    }

    public static final class Builder {
        private FundingSource source;
        private String code;

        public Funding build() {
            return new Funding(this);
        }

        public Builder withSource(FundingSource source) {
            this.source = source;
            return this;
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }
    }
}
