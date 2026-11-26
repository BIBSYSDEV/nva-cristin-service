package no.unit.nva.clinicaltrial.model.write;

import java.util.List;
import no.unit.nva.clinicaltrial.model.Entity;

public interface Write {

  List<Entity> entities();

  Writer writer();
}
