package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class Binary {

    public static final String BASE_64_DATA = "base64Data";

    @JsonProperty(BASE_64_DATA)
    private final String base64Data;

    public Binary(@JsonProperty(BASE_64_DATA) String base64Data) {
        this.base64Data = base64Data;
    }

    public String getBase64Data() {
        return base64Data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Binary)) {
            return false;
        }
        Binary binary = (Binary) o;
        return Objects.equals(getBase64Data(), binary.getBase64Data());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBase64Data());
    }
}
