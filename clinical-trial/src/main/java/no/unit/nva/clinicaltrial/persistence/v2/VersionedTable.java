package no.unit.nva.clinicaltrial.persistence.v2;

import static software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.sortBeginsWith;

import java.time.Instant;
import java.util.List;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Entity;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.clinicaltrial.model.Write;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

/** Every write through this table persists the item and a version of it in one transaction. */
public class VersionedTable {

  private final transient DynamoDbEnhancedClient client;
  private final transient DynamoDbTable<VersionDao> versions;
  private final transient DynamoDbTable<ClinicalTrialDao> clinicalTrials;

  public VersionedTable(DynamoDbEnhancedClient client, String tableName) {
    this.client = client;
    this.versions = client.table(tableName, TableSchema.fromBean(VersionDao.class));
    this.clinicalTrials = client.table(tableName, TableSchema.fromBean(ClinicalTrialDao.class));
  }

  public DynamoDbTable<ClinicalTrialDao> clinicalTrialTable() {
    return clinicalTrials;
  }

  /** Writes every entity of the write and a version of each, in one transaction or not at all. */
  public void save(Write write) throws ConditionFailedException, ItemNotFoundException {
    var timestamp = Instant.now();
    List<VersionedItem<?, ?>> items =
        write.entities().stream()
            .<VersionedItem<?, ?>>map(entity -> itemOf(entity, timestamp))
            .toList();
    var transaction = TransactWriteItemsEnhancedRequest.builder();
    items.forEach(item -> {
      item.addTo(transaction);
      transaction.addPutItem(versions, item.versionDao(write.writer(), timestamp));
    });
    try {
      client.transactWriteItems(transaction.build());
    } catch (TransactionCanceledException cancelled) {
      TransactionFailure.from(cancelled, items).throwWriteFailure();
    }
  }

  private VersionedItem<?, ?> itemOf(Entity entity, Instant timestamp) {
    return switch (entity) {
      case ClinicalTrial trial ->
          new VersionedItem<>(clinicalTrials, ClinicalTrialDao.from(trial, timestamp));
    };
  }

  public <T extends Entity> List<Version<T>> versionsOf(VersionedDao<T> key, Class<T> entityType) {
    return versions.query(versionsRequest(key)).items().stream()
        .map(versionDao -> versionDao.toVersion(entityType))
        .toList();
  }

  private static QueryEnhancedRequest versionsRequest(VersionedDao<?> key) {
    return QueryEnhancedRequest.builder()
        .queryConditional(
            sortBeginsWith(
                Key.builder()
                    .partitionValue(key.getPartitionKey())
                    .sortValue(VersionDao.keyPrefixOf(key))
                    .build()))
        .scanIndexForward(false)
        .build();
  }
}
