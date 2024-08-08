package no.unit.nva.cristin.person.model.nva;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Binary(@JsonProperty(BASE_64_DATA) String base64Data) {

    public static final String BASE_64_DATA = "base64Data";

}
