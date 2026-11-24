package no.unit.nva.clinicaltrial.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Reportable {
  YES("Yes"),
  NO("No");

  private final String value;

  Reportable(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
