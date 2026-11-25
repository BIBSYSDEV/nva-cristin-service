package no.unit.nva.clinicaltrial.model;

import java.util.List;
import java.util.Objects;

public record Submission(
    String projectIdentifier,
    String periodIdentifier,
    List<Inclusion> inclusions,
    SubmissionStatus status,
    Long version)
    implements Entity {

  public Submission {
    inclusions = Objects.isNull(inclusions) ? List.of() : List.copyOf(inclusions);
  }

  @Override
  public Action action() {
    return Objects.isNull(version) ? Action.CREATE_SUBMISSION : Action.UPDATE_SUBMISSION;
  }
}
