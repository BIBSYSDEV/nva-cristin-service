package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonProperty;

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

}
