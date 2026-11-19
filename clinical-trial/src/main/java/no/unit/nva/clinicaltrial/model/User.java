package no.unit.nva.clinicaltrial.model;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import nva.commons.apigateway.AccessRight;

public record User(
    String username,
    String personIdentifier,
    UUID customerIdentifier,
    Set<AccessRight> accessRights) {

  public User {
    accessRights = Objects.isNull(accessRights) ? Set.of() : Set.copyOf(accessRights);
  }
}
