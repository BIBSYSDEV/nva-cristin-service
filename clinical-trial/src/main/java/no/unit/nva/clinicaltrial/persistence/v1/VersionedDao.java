package no.unit.nva.clinicaltrial.persistence.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import no.unit.nva.clinicaltrial.model.Entity;

/** A row that carries the version its entity was written at. */
public interface VersionedDao<E extends Entity> extends Dao {

  Long version();

  /** The entity as this row describes it; its version is the one the write was based on. */
  E toEntity();

  @JsonIgnore
  default boolean isCreate() {
    return Objects.isNull(version());
  }

  default long nextVersion() {
    return isCreate() ? 1L : version() + 1;
  }
}
