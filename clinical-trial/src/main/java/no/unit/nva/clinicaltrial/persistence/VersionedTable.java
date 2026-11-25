package no.unit.nva.clinicaltrial.persistence;

import static software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.sortBeginsWith;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import no.unit.nva.clinicaltrial.model.Entity;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.clinicaltrial.model.write.Write;
import no.unit.nva.clinicaltrial.persistence.dao.Dao;
import no.unit.nva.clinicaltrial.persistence.dao.VersionDao;
import no.unit.nva.clinicaltrial.persistence.dao.VersionedDao;
import no.unit.nva.clinicaltrial.persistence.exceptions.ConditionFailedException;
import no.unit.nva.clinicaltrial.persistence.exceptions.ItemNotFoundException;
import no.unit.nva.clinicaltrial.persistence.exceptions.TransactionFailure;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

public class VersionedTable {

  private final transient DynamoDbEnhancedClient client;
  private final transient DynamoDbTable<EnhancedDocument> table;

  public VersionedTable(DynamoDbEnhancedClient client, String tableName) {
    this.client = client;
    this.table = client.table(tableName, Dao.SCHEMA);
  }

  public void save(Write write) throws ConditionFailedException, ItemNotFoundException {
    var timestamp = Instant.now();
    try {
      client.transactWriteItems(transactionOf(write, timestamp));
    } catch (TransactionCanceledException cancelled) {
      TransactionFailure.from(cancelled).throwWriteFailure();
    }
  }

  private static List<VersionedDao<?>> daosToPersist(List<Entity> entities, Instant timestamp) {
    return entities.stream()
        .<VersionedDao<?>>map(entity -> VersionedDao.fromEntity(entity, timestamp))
        .toList();
  }

  private TransactWriteItemsEnhancedRequest transactionOf(Write write, Instant timestamp) {
    var transaction = TransactWriteItemsEnhancedRequest.builder();
    daosToPersist(write.entities(), timestamp)
        .forEach(dao -> {
          transaction.addUpdateItem(table, dao.toUpdateRequest());
          transaction.addPutItem(table, dao.toVersionDao(write.writer()).toDocument());
        });
    return transaction.build();
  }

  public Optional<EnhancedDocument> item(Key key) {
    return Optional.ofNullable(
        table.getItem(GetItemEnhancedRequest.builder().key(key).consistentRead(true).build()));
  }

  public <T> List<Version<T>> versionsOf(Key entityKey, Class<T> type) {
    return table.query(versionsRequest(entityKey)).items().stream()
        .map(document -> Dao.<VersionDao<T>>fromDocument(document, VersionDao.class, type))
        .map(VersionDao::toVersion)
        .toList();
  }

  private static QueryEnhancedRequest versionsRequest(Key entityKey) {
    return QueryEnhancedRequest.builder()
        .queryConditional(
            sortBeginsWith(
                Key.builder()
                    .partitionValue(entityKey.partitionKeyValue())
                    .sortValue(VersionDao.keyPrefixOf(sortKeyOf(entityKey)))
                    .build()))
        .scanIndexForward(false)
        .build();
  }

  private static String sortKeyOf(Key entityKey) {
    return entityKey.sortKeyValue().orElseThrow().s();
  }
}
