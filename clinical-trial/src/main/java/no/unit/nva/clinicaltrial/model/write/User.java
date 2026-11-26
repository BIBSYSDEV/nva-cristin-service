package no.unit.nva.clinicaltrial.model.write;

import static java.util.stream.Collectors.toSet;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import no.unit.nva.clinicaltrial.model.Grant;

public record User(
    String username, String personIdentifier, UUID customerIdentifier, Set<Grant> grants)
    implements Writer {

  @Override
  public String name() {
    return username;
  }

  @Override
  public WriterKind kind() {
    return WriterKind.USER;
  }

  public User {
    grants = Objects.isNull(grants) ? Set.of() : Set.copyOf(grants);
  }

  public User withGrant(Grant grant) {
    return new User(
        username,
        personIdentifier,
        customerIdentifier,
        Stream.concat(grants.stream(), Stream.of(grant)).collect(toSet()));
  }

  public boolean isGranted() {
    return !grants.isEmpty();
  }
}
