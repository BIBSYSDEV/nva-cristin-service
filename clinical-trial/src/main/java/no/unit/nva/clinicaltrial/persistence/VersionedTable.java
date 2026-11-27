package no.unit.nva.clinicaltrial.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import no.unit.nva.clinicaltrial.model.Action;
import no.unit.nva.clinicaltrial.model.Entity;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.clinicaltrial.model.write.Actor;
import no.unit.nva.clinicaltrial.persistence.dao.Dao;
import no.unit.nva.clinicaltrial.persistence.dao.TableAttributes;
import no.unit.nva.clinicaltrial.persistence.dao.VersionDao;
import no.unit.nva.clinicaltrial.persistence.dao.VersionedDao;
import no.unit.nva.clinicaltrial.persistence.exceptions.ConditionFailedException;
import no.unit.nva.clinicaltrial.persistence.exceptions.ItemNotFoundException;
import no.unit.nva.clinicaltrial.persistence.exceptions.TransactionFailure;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ReturnValuesOnConditionCheckFailure;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

public class VersionedTable {

  private static final String EXPECTED_VERSION = ":expectedVersion";
  private static final String CREATE_CONDITION =
      "attribute_not_exists(" + TableAttributes.PARTITION_KEY + ")";
  private static final Expression CREATE_EXPRESSION =
      Expression.builder().expression(CREATE_CONDITION).build();
  private static final String VERSION_NAME = "#version";
  private static final String UPDATE_CONDITION = VERSION_NAME + " = " + EXPECTED_VERSION;
  private static final long FIRST_VERSION = 1L;

  private final transient DynamoDbEnhancedClient client;
  private final transient DynamoDbTable<EnhancedDocument> table;

  public VersionedTable(DynamoDbEnhancedClient client, String tableName) {
    this.client = client;
    this.table = client.table(tableName, Dao.SCHEMA);
  }

  public void save(Actor actor, Entity... entities)
      throws ConditionFailedException, ItemNotFoundException {
    try {
      client.transactWriteItems(transactionOf(List.of(entities), actor));
    } catch (TransactionCanceledException cancelled) {
      TransactionFailure.from(cancelled).throwWriteFailure();
    }
  }

  private TransactWriteItemsEnhancedRequest transactionOf(List<Entity> entities, Actor actor) {
    var transaction =
        TransactWriteItemsEnhancedRequest.builder()
            .clientRequestToken(UUID.randomUUID().toString());
    var timestamp = Instant.now();
    entities.stream()
        .flatMap(entity -> rowsOf(entity, actor, timestamp))
        .forEach(row -> transaction.addPutItem(table, row));
    return transaction.build();
  }

  private static Stream<TransactPutItemEnhancedRequest<EnhancedDocument>> rowsOf(
      Entity entity, Actor actor, Instant timestamp) {
    var dao = VersionedDao.fromEntity(entity, nextVersion(entity.version()), timestamp);
    var versionDao = VersionDao.versionOf(dao, Action.from(entity), actor);
    return Stream.of(
        putRequest(dao.toDocument(), condition(entity.version())),
        putRequest(versionDao.toDocument(), CREATE_EXPRESSION));
  }

  private static TransactPutItemEnhancedRequest<EnhancedDocument> putRequest(
      EnhancedDocument document, Expression condition) {
    return TransactPutItemEnhancedRequest.builder(EnhancedDocument.class)
        .item(document)
        .conditionExpression(condition)
        .returnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
        .build();
  }

  private static Expression condition(Long expectedVersion) {
    return Objects.isNull(expectedVersion) ? CREATE_EXPRESSION : updateExpression(expectedVersion);
  }

  private static Expression updateExpression(Long expectedVersion) {
    return Expression.builder()
        .expression(UPDATE_CONDITION)
        .expressionNames(Map.of(VERSION_NAME, TableAttributes.VERSION_ATTRIBUTE))
        .expressionValues(
            Map.of(EXPECTED_VERSION, AttributeValue.fromN(Long.toString(expectedVersion))))
        .build();
  }

  private static long nextVersion(Long currentVersion) {
    return Objects.isNull(currentVersion) ? FIRST_VERSION : currentVersion + 1;
  }

  public <E extends Entity, D extends VersionedDao<E>> Optional<E> item(Key key, Class<D> daoType) {
    return Optional.ofNullable(
            table.getItem(GetItemEnhancedRequest.builder().key(key).consistentRead(true).build()))
        .map(document -> VersionedDao.entityOf(document, daoType));
  }

  public List<Dao> partition(Key partitionKey) {
    return table.query(partitionRequest(partitionKey)).items().stream()
        .map(document -> Dao.fromDocument(document, Dao.class))
        .toList();
  }

  private static QueryEnhancedRequest partitionRequest(Key partitionKey) {
    return QueryEnhancedRequest.builder()
        .queryConditional(QueryConditional.keyEqualTo(partitionKey))
        .consistentRead(true)
        .build();
  }

  public <T> List<Version<T>> versionsOf(Key entityKey, Class<T> type) {
    return table.query(versionsRequest(entityKey)).items().stream()
        .map(document ->
            Dao.<VersionDao<T>>fromDocument(document, VersionDao.class, type).toVersion())
        .toList();
  }

  private static QueryEnhancedRequest versionsRequest(Key entityKey) {
    return QueryEnhancedRequest.builder()
        .queryConditional(
            QueryConditional.keyEqualTo(
                Key.builder().partitionValue(VersionDao.historyPartitionOf(entityKey)).build()))
        .scanIndexForward(false)
        .consistentRead(true)
        .build();
  }
}
