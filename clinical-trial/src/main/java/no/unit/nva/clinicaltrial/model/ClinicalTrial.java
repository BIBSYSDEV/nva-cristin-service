package no.unit.nva.clinicaltrial.model;

import java.util.Objects;

public record ClinicalTrial(String projectIdentifier, Reportable reportable, Long version)
    implements Entity {

  @Override
  public String identifier() {
    return projectIdentifier;
  }

  @Override
  public Action action() {
    return Objects.isNull(version) ? Action.CREATE_CLINICAL_TRIAL : Action.UPDATE_CLINICAL_TRIAL;
  }
}
