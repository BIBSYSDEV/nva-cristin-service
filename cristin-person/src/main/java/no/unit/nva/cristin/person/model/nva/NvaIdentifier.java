package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class NvaIdentifier {

    private final String type;
    private final String value;

    @JsonCreator
    public NvaIdentifier(@JsonProperty("type") String type, @JsonProperty("value") String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NvaIdentifier)) {
            return false;
        }
        NvaIdentifier that = (NvaIdentifier) o;
        return getType().equals(that.getType()) && getValue().equals(that.getValue());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getType(), getValue());
    }

    @JacocoGenerated
    public static final class Builder {

        private transient String type;
        private transient String value;

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public NvaIdentifier build() {
            return new NvaIdentifier(this.type, this.value);
        }
    }
}
