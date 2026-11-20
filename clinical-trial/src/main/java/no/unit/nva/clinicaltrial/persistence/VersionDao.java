package no.unit.nva.clinicaltrial.persistence;

import static no.unit.nva.clinicaltrial.persistence.DatabaseConstants.KEY_SEPARATOR;
import static no.unit.nva.clinicaltrial.persistence.DatabaseConstants.PARTITION_KEY;
import static no.unit.nva.clinicaltrial.persistence.DatabaseConstants.SORT_KEY;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import no.unit.nva.clinicaltrial.model.User;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.identifiers.SortableIdentifier;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public record VersionDao<T>(
    @JsonProperty(PARTITION_KEY) String partitionKey,
    @JsonProperty(SORT_KEY) String sortKey,
    T entity,
    String username,
    UUID customerIdentifier,
    Instant createdDate,
    Instant modifiedDate)
    implements Dao {

  private static final String VERSION_KEY_PREFIX = "VERSION" + KEY_SEPARATOR;
  private static final String PARTITION_KEY_VALUE = ":partitionKey";
  private static final String VERSION_PREFIX_VALUE = ":versionPrefix";
  public static final String VERSIONS_KEY_CONDITION =
      PARTITION_KEY
          + " = "
          + PARTITION_KEY_VALUE
          + " AND begins_with("
          + SORT_KEY
          + ", "
          + VERSION_PREFIX_VALUE
          + ")";

  public static <T> VersionDao<T> fromDao(
      Dao dao, T entity, User user, SortableIdentifier version, Instant timestamp) {
    return new VersionDao<>(
        dao.partitionKey(),
        versionKey(dao.sortKey(), version),
        entity,
        user.username(),
        user.customerIdentifier(),
        timestamp,
        timestamp);
  }

  public static <T> VersionDao<T> fromItem(Map<String, AttributeValue> item, Class<T> entityType) {
    return Dao.fromItem(item, VersionDao.class, entityType);
  }

  public static Map<String, AttributeValue> versionsConditionValues(
      Map<String, AttributeValue> entityKey) {
    return Map.of(
        PARTITION_KEY_VALUE,
        entityKey.get(PARTITION_KEY),
        VERSION_PREFIX_VALUE,
        string(versionKeyPrefix(entityKey.get(SORT_KEY).s())));
  }

  public Version<T> toVersion() {
    return new Version<>(
        identifier(), entity, new User(username, customerIdentifier), modifiedDate);
  }

  private static String versionKey(String entitySortKey, SortableIdentifier version) {
    return versionKeyPrefix(entitySortKey) + version;
  }

  private static String versionKeyPrefix(String entitySortKey) {
    return VERSION_KEY_PREFIX + entitySortKey + KEY_SEPARATOR;
  }

  private SortableIdentifier identifier() {
    return new SortableIdentifier(sortKey.substring(sortKey.lastIndexOf(KEY_SEPARATOR) + 1));
  }

  private static AttributeValue string(String value) {
    return AttributeValue.builder().s(value).build();
  }
}
