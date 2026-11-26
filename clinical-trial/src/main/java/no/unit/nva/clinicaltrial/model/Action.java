package no.unit.nva.clinicaltrial.model;

import static java.util.Objects.isNull;

public enum Action {
  CREATE,
  UPDATE;

  public static Action from(Entity entity) {
    return isNull(entity.version()) ? CREATE : UPDATE;
  }
}
