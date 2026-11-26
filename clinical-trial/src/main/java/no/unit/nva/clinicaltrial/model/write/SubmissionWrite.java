package no.unit.nva.clinicaltrial.model.write;

import java.util.List;
import no.unit.nva.clinicaltrial.model.Entity;
import no.unit.nva.clinicaltrial.model.Submission;

public record SubmissionWrite(Submission submission, User user) implements Write {

  @Override
  public List<Entity> entities() {
    return List.of(submission);
  }

  @Override
  public Writer writer() {
    return user;
  }
}
