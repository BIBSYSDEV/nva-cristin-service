package no.unit.nva.clinicaltrial.persistence.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.Instant;
import no.unit.nva.clinicaltrial.model.Action;
import no.unit.nva.clinicaltrial.model.Entity;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.clinicaltrial.model.write.Writer;
import no.unit.nva.identifiers.SortableIdentifier;

@JsonTypeName("Version")
public record VersionDao<T>(
    @JsonProperty(PARTITION_KEY) String partitionKey,
    @JsonProperty(SORT_KEY) String sortKey,
    T entity,
    Writer writer,
    Action action,
    Instant createdDate,
    Instant modifiedDate)
    implements Dao {

  private static final String VERSION_KEY_PREFIX = "VERSION#";
  private static final String KEY_SEPARATOR = "#";

  public static <E extends Entity> VersionDao<E> versionOf(
      VersionedDao<E> entityDao, Writer writer) {
    var action = entityDao.toEntity().action();
    var entity = entityDao.entityAtVersion(entityDao.nextVersion());
    var timestamp = entityDao.modifiedDate();
    return new VersionDao<>(
        entityDao.partitionKey(),
        keyPrefixOf(entityDao.sortKey()) + SortableIdentifier.next(),
        entity,
        writer,
        action,
        timestamp,
        timestamp);
  }

  public static String keyPrefixOf(String entitySortKey) {
    return VERSION_KEY_PREFIX + entitySortKey + KEY_SEPARATOR;
  }

  public Version<T> toVersion() {
    return new Version<>(identifier(), entity, writer, action, modifiedDate);
  }

  private SortableIdentifier identifier() {
    return new SortableIdentifier(sortKey.substring(sortKey.lastIndexOf(KEY_SEPARATOR) + 1));
  }
}
