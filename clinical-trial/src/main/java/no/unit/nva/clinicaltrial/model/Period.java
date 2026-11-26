package no.unit.nva.clinicaltrial.model;

import java.util.Objects;

public record Period(String identifier, Long version) implements Entity {

  @Override
  public Action action() {
    return Objects.isNull(version) ? Action.CREATE_PERIOD : Action.UPDATE_PERIOD;
  }
}
