package no.unit.nva.clinicaltrial.persistence.v2;

import java.time.Instant;
import java.util.Objects;
import no.unit.nva.clinicaltrial.model.Entity;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public abstract class VersionedDao<E extends Entity> extends Dao {

  private Long version;

  /** The entity as this row describes it; its version is the one the write was based on. */
  public abstract E toEntity();

  /** An updated item keeps the created date it was given when it was created. */
  @Override
  public void timestamp(Instant timestamp) {
    setModifiedDate(timestamp);
    setCreatedDate(isCreate() ? timestamp : null);
  }

  public boolean isCreate() {
    return Objects.isNull(version);
  }

  @DynamoDbVersionAttribute
  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
