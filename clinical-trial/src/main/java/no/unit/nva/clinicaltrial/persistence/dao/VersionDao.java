package no.unit.nva.clinicaltrial.persistence.dao;

import static no.unit.nva.clinicaltrial.persistence.dao.TableAttributes.KEY_SEPARATOR;
import static no.unit.nva.clinicaltrial.persistence.dao.TableAttributes.PARTITION_KEY;
import static no.unit.nva.clinicaltrial.persistence.dao.TableAttributes.SORT_KEY;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.Instant;
import no.unit.nva.clinicaltrial.model.Action;
import no.unit.nva.clinicaltrial.model.Entity;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.clinicaltrial.model.write.Writer;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@JsonTypeName("Version")
public record VersionDao<T>(
    @JsonProperty(PARTITION_KEY) String partitionKey,
    @JsonProperty(SORT_KEY) String sortKey,
    String entityType,
    T entity,
    Writer writer,
    Action action,
    Instant modifiedDate)
    implements Dao {

  private static final String HISTORY_KEY_PREFIX = "HISTORY";
  private static final String VERSION_KEY_FORMAT = "%019d";

  public static <E extends Entity> VersionDao<E> versionOf(
      VersionedDao<E> entityDao, Action action, Writer writer) {
    var entity = entityDao.toEntity();
    return new VersionDao<>(
        historyPartitionOf(entityDao.partitionKey(), entityDao.sortKey()),
        sortKeyOf(entityDao.version()),
        entity.getClass().getSimpleName(),
        entity,
        writer,
        action,
        entityDao.modifiedDate());
  }

  private static String historyPartitionOf(String entityPartitionKey, String entitySortKey) {
    return HISTORY_KEY_PREFIX + KEY_SEPARATOR + entityPartitionKey + KEY_SEPARATOR + entitySortKey;
  }

  public static String historyPartitionOf(Key entityKey) {
    return historyPartitionOf(
        entityKey.partitionKeyValue().s(), entityKey.sortKeyValue().orElseThrow().s());
  }

  public Version<T> toVersion() {
    return new Version<>(entity, writer, action, modifiedDate);
  }

  private static String sortKeyOf(long version) {
    return VERSION_KEY_FORMAT.formatted(version);
  }
}
