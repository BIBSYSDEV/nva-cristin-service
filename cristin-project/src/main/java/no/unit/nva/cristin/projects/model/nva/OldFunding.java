package no.unit.nva.cristin.projects.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

public class OldFunding implements JsonSerializable {

    public static final String SOURCE = "source";
    public static final String CODE = "code";

    @JsonProperty(SOURCE)
    private final FundingSource source;
    @JsonProperty(CODE)
    private final String code;

    @JsonCreator
    public OldFunding(@JsonProperty(SOURCE) FundingSource source, @JsonProperty(CODE) String code) {
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
        if (this == o) {
            return true;
        }
        if (!(o instanceof OldFunding)) {
            return false;
        }
        OldFunding that = (OldFunding) o;
        return Objects.equals(getSource(), that.getSource()) && Objects.equals(getCode(), that.getCode());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getSource(), getCode());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

}
