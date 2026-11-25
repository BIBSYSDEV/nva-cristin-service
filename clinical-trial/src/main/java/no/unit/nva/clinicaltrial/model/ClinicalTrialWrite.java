package no.unit.nva.clinicaltrial.model;

import java.util.List;

public record ClinicalTrialWrite(ClinicalTrial entity, User user) implements Write {

  @Override
  public List<Entity> entities() {
    return List.of(entity);
  }

  @Override
  public Writer writer() {
    return user;
  }
}
