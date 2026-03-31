package no.unit.nva.cristin.person.model.nva;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Binary(@JsonProperty(BASE_64_DATA) String base64Data) {

  public static final String BASE_64_DATA = "base64Data";

  public static Binary empty() {
    return new Binary(null);
  }

  public boolean isEmpty() {
    return isNull(base64Data);
  }
}
