package no.unit.nva.clinicaltrial.persistence.v3;

import static software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.sortBeginsWith;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Entity;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.clinicaltrial.model.Write;
import no.unit.nva.identifiers.SortableIdentifier;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableMetadata;
import software.amazon.awssdk.enhanced.dynamodb.document.DocumentTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactUpdateItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ReturnValuesOnConditionCheckFailure;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

/** Every write through this table persists the item and a version of it in one transaction. */
public class VersionedTable {

  private static final String PARTITION_KEY = "PK";
  private static final String SORT_KEY = "SK";
  private static final String CREATE_CONDITION = "attribute_not_exists(PK)";
  private static final String UPDATE_CONDITION = "version = :expectedVersion";
  private static final String EXPECTED_VERSION = ":expectedVersion";

  private final transient DynamoDbEnhancedClient client;
  private final transient DynamoDbTable<EnhancedDocument> table;

  public VersionedTable(DynamoDbEnhancedClient client, String tableName) {
    this.client = client;
    this.table = client.table(tableName, documentSchema());
  }

  /** Writes every entity of the write and a version of each, in one transaction or not at all. */
  public void save(Write write) throws ConditionFailedException, ItemNotFoundException {
    var timestamp = Instant.now();
    List<VersionedDao<?>> daos =
        write.entities().stream().<VersionedDao<?>>map(entity -> daoOf(entity, timestamp)).toList();
    var transaction = TransactWriteItemsEnhancedRequest.builder();
    var rows = new ArrayList<VersionedDao<?>>();
    daos.forEach(dao -> {
      transaction.addUpdateItem(table, entityItem(dao));
      transaction.addPutItem(table, versionDocument(dao, write, timestamp));
      rows.add(dao);
      rows.add(dao);
    });
    try {
      client.transactWriteItems(transaction.build());
    } catch (TransactionCanceledException cancelled) {
      TransactionFailure.from(cancelled, rows).throwWriteFailure();
    }
  }

  public Optional<EnhancedDocument> item(Key key) {
    return Optional.ofNullable(
        table.getItem(GetItemEnhancedRequest.builder().key(key).consistentRead(true).build()));
  }

  public <T> List<Version<T>> versionsOf(Key entityKey, String entitySortKey, Class<T> type) {
    return table
        .query(getQueryEnhancedRequest(entityKey, entitySortKey))
        .items()
        .stream()
        .map(document -> Dao.<VersionDao<T>>fromDocument(document, VersionDao.class, type))
        .map(VersionDao::toVersion)
        .toList();
  }

  private static QueryEnhancedRequest getQueryEnhancedRequest(Key entityKey, String entitySortKey) {
    return QueryEnhancedRequest.builder()
               .queryConditional(
                   sortBeginsWith(
                       Key.builder()
                           .partitionValue(entityKey.partitionKeyValue())
                           .sortValue(VersionDao.keyPrefixOf(entitySortKey))
                           .build()))
               .scanIndexForward(false)
               .build();
  }

  private static DocumentTableSchema documentSchema() {
    return DocumentTableSchema.builder()
        .addIndexPartitionKey(TableMetadata.primaryIndexName(), PARTITION_KEY, AttributeValueType.S)
        .addIndexSortKey(TableMetadata.primaryIndexName(), SORT_KEY, AttributeValueType.S)
        .build();
  }

  private static VersionedDao<?> daoOf(Entity entity, Instant timestamp) {
    return switch (entity) {
      case ClinicalTrial trial -> ClinicalTrialDao.from(trial, timestamp);
    };
  }

  private static TransactUpdateItemEnhancedRequest<EnhancedDocument> entityItem(
      VersionedDao<?> dao) {
    return TransactUpdateItemEnhancedRequest.builder(EnhancedDocument.class)
        .item(dao.toDocument().toBuilder().putNumber("version", dao.nextVersion()).build())
        .ignoreNulls(true)
        .conditionExpression(dao.isCreate() ? createCondition() : updateCondition(dao))
        .returnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
        .build();
  }

  private static EnhancedDocument versionDocument(
      VersionedDao<?> dao, Write write, Instant timestamp) {
    return VersionDao.versionOf(
            dao,
            SortableIdentifier.next(),
            dao.toEntity(),
            write.writer(),
            dao.toEntity().action(),
            timestamp)
        .toDocument();
  }

  private static Expression createCondition() {
    return Expression.builder().expression(CREATE_CONDITION).build();
  }

  private static Expression updateCondition(VersionedDao<?> dao) {
    return Expression.builder()
        .expression(UPDATE_CONDITION)
        .expressionValues(
            Map.of(EXPECTED_VERSION, AttributeValue.fromN(Long.toString(dao.version()))))
        .build();
  }
}
