package no.unit.nva.clinicaltrial.model.write;

import java.util.List;
import no.unit.nva.clinicaltrial.model.Entity;
import no.unit.nva.clinicaltrial.model.Period;

public record PeriodWrite(Period period, Writer writer) implements Write {

  @Override
  public List<Entity> entities() {
    return List.of(period);
  }
}
