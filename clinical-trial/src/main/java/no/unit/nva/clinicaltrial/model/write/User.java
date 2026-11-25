package no.unit.nva.clinicaltrial.model.write;

import static java.util.stream.Collectors.toSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import no.unit.nva.clinicaltrial.model.Grant;
import no.unit.nva.identifiers.CustomerIdentifier;
import no.unit.nva.identifiers.PersonIdentifier;

public record User(
    String username,
    PersonIdentifier personIdentifier,
    CustomerIdentifier customerIdentifier,
    Set<Grant> grants)
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

  @JsonIgnore
  public boolean isGranted() {
    return !grants.isEmpty();
  }
}
