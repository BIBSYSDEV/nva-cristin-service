package no.unit.nva.clinicaltrial.persistence.v1;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Entity;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.clinicaltrial.model.Write;
import no.unit.nva.identifiers.SortableIdentifier;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.ReturnValuesOnConditionCheckFailure;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;
import software.amazon.awssdk.services.dynamodb.model.Update;

/** Every write through this table persists the item and a version of it in one transaction. */
public class VersionedTable {

  private static final String PARTITION_KEY = "PK";
  private static final String SORT_KEY = "SK";
  private static final String VERSION = "version";
  private static final String CREATE_CONDITION = "attribute_not_exists(PK)";
  private static final String UPDATE_CONDITION = "version = :expectedVersion";
  private static final String EXPECTED_VERSION = ":expectedVersion";
  private static final String VERSIONS_CONDITION =
      "PK = :partitionKey AND begins_with(SK, :versionPrefix)";
  private static final String CONDITIONAL_CHECK_FAILED = "ConditionalCheckFailed";
  private static final String SET = "SET ";
  private static final String ASSIGNMENT = "#%s = :%s";
  private static final String SEPARATOR = ", ";
  private static final Set<String> KEYS = Set.of(PARTITION_KEY, SORT_KEY);

  private final transient DynamoDbClient client;
  private final transient String tableName;

  public VersionedTable(DynamoDbClient client, String tableName) {
    this.client = client;
    this.tableName = tableName;
  }

  /** Writes every entity of the write and a version of each, in one transaction or not at all. */
  public void save(Write write) throws ConditionFailedException, ItemNotFoundException {
    var timestamp = Instant.now();
    List<VersionedDao<?>> daos =
        write.entities().stream().<VersionedDao<?>>map(entity -> daoOf(entity, timestamp)).toList();
    var items = new ArrayList<TransactWriteItem>();
    daos.forEach(dao -> {
      items.add(entityItem(dao));
      items.add(versionItem(dao, write, timestamp));
    });
    try {
      client.transactWriteItems(TransactWriteItemsRequest.builder().transactItems(items).build());
    } catch (TransactionCanceledException cancelled) {
      throw writeFailure(cancelled, daos);
    }
  }

  public Map<String, AttributeValue> item(Map<String, AttributeValue> key) {
    return client
        .getItem(
            GetItemRequest.builder().tableName(tableName).key(key).consistentRead(true).build())
        .item();
  }

  public <T> List<Version<T>> versionsOf(Map<String, AttributeValue> entityKey, Class<T> type) {
    return client
        .query(
            QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression(VERSIONS_CONDITION)
                .expressionAttributeValues(VersionDao.versionsConditionValues(entityKey))
                .scanIndexForward(false)
                .build())
        .items()
        .stream()
        .map(item -> Dao.<VersionDao<T>>fromItem(item, VersionDao.class, type))
        .map(VersionDao::toVersion)
        .toList();
  }

  private static VersionedDao<?> daoOf(Entity entity, Instant timestamp) {
    return switch (entity) {
      case ClinicalTrial trial -> ClinicalTrialDao.from(trial, timestamp);
    };
  }

  private TransactWriteItem entityItem(VersionedDao<?> dao) {
    var attributes = attributesToSet(dao);
    return TransactWriteItem.builder()
        .update(
            Update.builder()
                .tableName(tableName)
                .key(keyOf(dao))
                .updateExpression(assignmentsOf(attributes))
                .conditionExpression(dao.isCreate() ? CREATE_CONDITION : UPDATE_CONDITION)
                .expressionAttributeNames(namesOf(attributes))
                .expressionAttributeValues(valuesOf(attributes, dao))
                .returnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
                .build())
        .build();
  }

  private static Map<String, AttributeValue> attributesToSet(VersionedDao<?> dao) {
    var attributes = new HashMap<>(dao.toItem());
    KEYS.forEach(attributes::remove);
    attributes.put(VERSION, numberOf(dao.nextVersion()));
    return attributes;
  }

  private static String assignmentsOf(Map<String, AttributeValue> attributes) {
    return SET
        + attributes.keySet().stream()
            .map(name -> ASSIGNMENT.formatted(name, name))
            .collect(joining(SEPARATOR));
  }

  private static Map<String, String> namesOf(Map<String, AttributeValue> attributes) {
    return attributes.keySet().stream().collect(toMap(name -> "#" + name, name -> name));
  }

  private static Map<String, AttributeValue> valuesOf(
      Map<String, AttributeValue> attributes, VersionedDao<?> dao) {
    var values = new HashMap<String, AttributeValue>();
    attributes.forEach((name, value) -> values.put(":" + name, value));
    if (!dao.isCreate()) {
      values.put(EXPECTED_VERSION, numberOf(dao.version()));
    }
    return values;
  }

  private static Map<String, AttributeValue> keyOf(VersionedDao<?> dao) {
    return Map.of(
        PARTITION_KEY, AttributeValue.fromS(dao.partitionKey()),
        SORT_KEY, AttributeValue.fromS(dao.sortKey()));
  }

  private static AttributeValue numberOf(long value) {
    return AttributeValue.fromN(Long.toString(value));
  }

  private TransactWriteItem versionItem(VersionedDao<?> dao, Write write, Instant timestamp) {
    var versionDao =
        VersionDao.versionOf(
            dao,
            SortableIdentifier.next(),
            dao.toEntity(),
            write.writer(),
            dao.toEntity().action(),
            timestamp);
    return TransactWriteItem.builder()
        .put(Put.builder().tableName(tableName).item(versionDao.toItem()).build())
        .build();
  }

  private static ItemNotFoundException writeFailure(
      TransactionCanceledException cancelled, List<VersionedDao<?>> daos)
      throws ConditionFailedException {
    var reasons = cancelled.cancellationReasons();
    var failed =
        IntStream.range(0, reasons.size())
            .filter(index -> CONDITIONAL_CHECK_FAILED.equals(reasons.get(index).code()))
            .findFirst()
            .orElseThrow(() -> cancelled);
    if (daos.get(failed / 2).isCreate() || reasons.get(failed).hasItem()) {
      throw new ConditionFailedException();
    }
    return new ItemNotFoundException();
  }
}
