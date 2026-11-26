package no.unit.nva.clinicaltrial.model.write;

import java.util.List;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Entity;

public record ClinicalTrialWrite(ClinicalTrial clinicalTrial, User user) implements Write {

  @Override
  public List<Entity> entities() {
    return List.of(clinicalTrial);
  }

  @Override
  public Writer writer() {
    return user;
  }
}
