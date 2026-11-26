package no.unit.nva.clinicaltrial.persistence.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.Instant;
import no.unit.nva.clinicaltrial.model.Period;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@JsonTypeName("Period")
public record PeriodDao(
    @JsonProperty(PARTITION_KEY) String partitionKey,
    @JsonProperty(SORT_KEY) String sortKey,
    Long version,
    Instant modifiedDate)
    implements VersionedDao<Period> {

  private static final String PERIOD_PARTITION_KEY = "PERIOD";

  public static PeriodDao from(Period period, long version, Instant modifiedDate) {
    return new PeriodDao(PERIOD_PARTITION_KEY, period.identifier(), version, modifiedDate);
  }

  public static Key keyOf(String identifier) {
    return Key.builder().partitionValue(PERIOD_PARTITION_KEY).sortValue(identifier).build();
  }

  @Override
  public Period toEntity() {
    return new Period(sortKey, version);
  }
}
