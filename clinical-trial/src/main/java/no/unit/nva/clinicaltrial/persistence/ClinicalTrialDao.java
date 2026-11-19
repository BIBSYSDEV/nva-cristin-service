package no.unit.nva.clinicaltrial.persistence;

import java.time.Instant;
import java.util.Map;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;

public record ClinicalTrialDao(
    String partitionKey, String sortKey, Instant createdDate, Instant modifiedDate) implements Dao {

  private static final String PARTITION_KEY = "PK";
  private static final String SORT_KEY = "SK";
  private static final String CREATED_DATE = "createdDate";
  private static final String MODIFIED_DATE = "modifiedDate";
  private static final String PROJECT_KEY_PREFIX = "PROJECT#";
  private static final String CLINICAL_TRIAL_SORT_KEY = "CLINICAL_TRIAL";

  private static final String NEW_ITEM_CONDITION_EXPRESSION =
      "attribute_not_exists(" + PARTITION_KEY + ")";

  public static ClinicalTrialDao fromClinicalTrial(ClinicalTrial clinicalTrial, Instant timestamp) {
    return new ClinicalTrialDao(
        projectKey(clinicalTrial.projectIdentifier()),
        CLINICAL_TRIAL_SORT_KEY,
        timestamp,
        timestamp);
  }

  public static ClinicalTrialDao fromItem(Map<String, AttributeValue> item) {
    return new ClinicalTrialDao(
        item.get(PARTITION_KEY).s(),
        item.get(SORT_KEY).s(),
        Instant.parse(item.get(CREATED_DATE).s()),
        Instant.parse(item.get(MODIFIED_DATE).s()));
  }

  public static Map<String, AttributeValue> keyOf(String projectIdentifier) {
    return Map.of(
        PARTITION_KEY,
        string(projectKey(projectIdentifier)),
        SORT_KEY,
        string(CLINICAL_TRIAL_SORT_KEY));
  }

  @Override
  public TransactWriteItem toTransactWriteItem(String tableName) {
    return TransactWriteItem.builder().put(put(tableName)).build();
  }

  private Put put(String tableName) {
    return Put.builder()
        .tableName(tableName)
        .item(toItem())
        .conditionExpression(NEW_ITEM_CONDITION_EXPRESSION)
        .build();
  }

  private Map<String, AttributeValue> toItem() {
    return Map.of(
        PARTITION_KEY, string(partitionKey),
        SORT_KEY, string(sortKey),
        CREATED_DATE, string(createdDate.toString()),
        MODIFIED_DATE, string(modifiedDate.toString()));
  }

  public ClinicalTrial toClinicalTrial() {
    return new ClinicalTrial(partitionKey.substring(PROJECT_KEY_PREFIX.length()));
  }

  private static String projectKey(String projectIdentifier) {
    return PROJECT_KEY_PREFIX + projectIdentifier;
  }

  private static AttributeValue string(String value) {
    return AttributeValue.builder().s(value).build();
  }
}
