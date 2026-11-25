package no.unit.nva.clinicaltrial.persistence.v3;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import no.unit.nva.clinicaltrial.model.Action;
import no.unit.nva.clinicaltrial.model.Grant;
import no.unit.nva.clinicaltrial.model.SystemWriter;
import no.unit.nva.clinicaltrial.model.User;
import no.unit.nva.clinicaltrial.model.Version;
import no.unit.nva.clinicaltrial.model.Writer;
import no.unit.nva.clinicaltrial.model.WriterKind;
import no.unit.nva.identifiers.SortableIdentifier;

public record VersionDao<T>(
    @JsonProperty("PK") String partitionKey,
    @JsonProperty("SK") String sortKey,
    T entity,
    String writer,
    WriterKind writerKind,
    String personIdentifier,
    UUID customerIdentifier,
    Set<Grant> grants,
    Action action,
    Instant createdDate,
    Instant modifiedDate)
    implements Dao {

  private static final String VERSION_KEY_PREFIX = "VERSION#";
  private static final String KEY_SEPARATOR = "#";

  public static <T> VersionDao<T> versionOf(
      Dao entityDao,
      SortableIdentifier identifier,
      T entity,
      Writer writer,
      Action action,
      Instant timestamp) {
    var user = writer instanceof User person ? person : null;
    return new VersionDao<>(
        entityDao.partitionKey(),
        keyPrefixOf(entityDao.sortKey()) + identifier,
        entity,
        writer.name(),
        writer.kind(),
        user == null ? null : user.personIdentifier(),
        user == null ? null : user.customerIdentifier(),
        user == null ? null : user.grants(),
        action,
        timestamp,
        timestamp);
  }

  public static String keyPrefixOf(String entitySortKey) {
    return VERSION_KEY_PREFIX + entitySortKey + KEY_SEPARATOR;
  }

  public Version<T> toVersion() {
    return new Version<>(identifier(), entity, toWriter(), action, modifiedDate);
  }

  private SortableIdentifier identifier() {
    return new SortableIdentifier(sortKey.substring(sortKey.lastIndexOf(KEY_SEPARATOR) + 1));
  }

  private Writer toWriter() {
    return WriterKind.USER == writerKind
        ? new User(writer, personIdentifier, customerIdentifier, grants)
        : new SystemWriter(writer);
  }
}
