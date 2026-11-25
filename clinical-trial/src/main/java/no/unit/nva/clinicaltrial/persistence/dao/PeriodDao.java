package no.unit.nva.clinicaltrial.persistence.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.Instant;
import java.util.Objects;
import no.unit.nva.clinicaltrial.model.Period;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@JsonTypeName("Period")
public record PeriodDao(
    @JsonProperty(PARTITION_KEY) String partitionKey,
    @JsonProperty(SORT_KEY) String sortKey,
    Long version,
    Instant createdDate,
    Instant modifiedDate)
    implements VersionedDao<Period> {

  private static final String PERIOD_PARTITION_KEY = "PERIOD";

  public static PeriodDao from(Period period, Instant timestamp) {
    return new PeriodDao(
        PERIOD_PARTITION_KEY,
        period.identifier(),
        period.version(),
        Objects.isNull(period.version()) ? timestamp : null,
        timestamp);
  }

  public static Key keyOf(String identifier) {
    return Key.builder().partitionValue(PERIOD_PARTITION_KEY).sortValue(identifier).build();
  }

  @Override
  public Period entityAtVersion(Long version) {
    return new Period(sortKey, version);
  }
}
