package no.unit.nva.clinicaltrial.persistence;

import static no.unit.nva.clinicaltrial.persistence.DatabaseConstants.KEY_SEPARATOR;
import static no.unit.nva.clinicaltrial.persistence.DatabaseConstants.PARTITION_KEY;
import static no.unit.nva.clinicaltrial.persistence.DatabaseConstants.SORT_KEY;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.identifiers.SortableIdentifier;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public record ClinicalTrialDao(
    @JsonProperty(PARTITION_KEY) String partitionKey,
    @JsonProperty(SORT_KEY) String sortKey,
    SortableIdentifier version,
    Instant createdDate,
    Instant modifiedDate)
    implements Dao {

  private static final String PROJECT_KEY_PREFIX = "PROJECT" + KEY_SEPARATOR;
  private static final String CLINICAL_TRIAL_SORT_KEY = "CLINICAL_TRIAL";
  private static final String VERSION = "version";
  private static final String EXPECTED_VERSION_VALUE = ":expectedVersion";

  public static final String CREATE_CONDITION = "attribute_not_exists(" + PARTITION_KEY + ")";
  public static final String UPDATE_CONDITION =
      "attribute_exists(" + PARTITION_KEY + ") AND " + VERSION + " = " + EXPECTED_VERSION_VALUE;

  public static ClinicalTrialDao fromClinicalTrial(ClinicalTrial clinicalTrial, Instant timestamp) {
    return new ClinicalTrialDao(
        projectKey(clinicalTrial.projectIdentifier()),
        CLINICAL_TRIAL_SORT_KEY,
        clinicalTrial.version(),
        timestamp,
        timestamp);
  }

  public static ClinicalTrialDao fromItem(Map<String, AttributeValue> item) {
    return Dao.fromItem(item, ClinicalTrialDao.class);
  }

  public static Map<String, AttributeValue> expectedVersionValues(
      SortableIdentifier expectedVersion) {
    return Map.of(EXPECTED_VERSION_VALUE, string(expectedVersion.toString()));
  }

  public static Map<String, AttributeValue> keyOf(String projectIdentifier) {
    return Map.of(
        PARTITION_KEY,
        string(projectKey(projectIdentifier)),
        SORT_KEY,
        string(CLINICAL_TRIAL_SORT_KEY));
  }

  public ClinicalTrialDao updatedTo(ClinicalTrial clinicalTrial, Instant timestamp) {
    return new ClinicalTrialDao(
        partitionKey, sortKey, clinicalTrial.version(), createdDate, timestamp);
  }

  public ClinicalTrial toClinicalTrial() {
    return new ClinicalTrial(partitionKey.substring(PROJECT_KEY_PREFIX.length()), version);
  }

  private static String projectKey(String projectIdentifier) {
    return PROJECT_KEY_PREFIX + projectIdentifier;
  }

  private static AttributeValue string(String value) {
    return AttributeValue.builder().s(value).build();
  }
}
