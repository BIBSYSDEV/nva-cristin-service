package no.unit.nva.clinicaltrial.persistence.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Reportable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public record ClinicalTrialDao(
    @JsonProperty("PK") String partitionKey,
    @JsonProperty("SK") String sortKey,
    Reportable reportable,
    Long version,
    Instant createdDate,
    Instant modifiedDate)
    implements VersionedDao<ClinicalTrial> {

  private static final String PROJECT_KEY_PREFIX = "PROJECT#";
  private static final String CLINICAL_TRIAL_SORT_KEY = "CLINICAL_TRIAL";

  public static ClinicalTrialDao from(ClinicalTrial clinicalTrial, Instant timestamp) {
    return new ClinicalTrialDao(
        partitionKeyOf(clinicalTrial.identifier()),
        CLINICAL_TRIAL_SORT_KEY,
        clinicalTrial.reportable(),
        clinicalTrial.version(),
        Objects.isNull(clinicalTrial.version()) ? timestamp : null,
        timestamp);
  }

  public static Map<String, AttributeValue> keyOf(String projectIdentifier) {
    return Map.of(
        "PK", AttributeValue.fromS(partitionKeyOf(projectIdentifier)),
        "SK", AttributeValue.fromS(CLINICAL_TRIAL_SORT_KEY));
  }

  @Override
  public ClinicalTrial toEntity() {
    return new ClinicalTrial(projectIdentifierOf(partitionKey), reportable, version);
  }

  private static String partitionKeyOf(String projectIdentifier) {
    return PROJECT_KEY_PREFIX + projectIdentifier;
  }

  private static String projectIdentifierOf(String partitionKey) {
    return partitionKey.substring(PROJECT_KEY_PREFIX.length());
  }
}
