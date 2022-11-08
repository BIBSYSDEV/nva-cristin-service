package no.unit.nva.cristin.projects.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nva.commons.core.JacocoGenerated;

import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Funding {

    public static final String SOURCE = "source";
    public static final String CODE = "code";

    @JsonProperty(SOURCE)
    private final FundingSource source;
    @JsonProperty(CODE)
    private final String code;

    @JsonCreator
    public Funding(@JsonProperty(SOURCE) FundingSource source, @JsonProperty(CODE) String code) {
        this.source = source;
        this.code = code;
    }

    public FundingSource getSource() {
        return source;
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
        if (!(o instanceof Funding)) {
            return false;
        }
        Funding that = (Funding) o;
        return getSource().equals(that.getSource()) && Objects.equals(getCode(), that.getCode());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getSource(), getCode());
    }

}
