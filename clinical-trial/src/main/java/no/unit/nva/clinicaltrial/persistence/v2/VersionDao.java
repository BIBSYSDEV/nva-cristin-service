package no.unit.nva.clinicaltrial.persistence.v2;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;

@DynamoDbBean
public class VersionDao extends Dao {

  private static final String VERSION_KEY_PREFIX = "VERSION#";
  private static final String KEY_SEPARATOR = "#";

  private static final String ENTITY_NOT_MAPPABLE = "Entity is not mappable";

  private JsonNode entity;
  private String writer;
  private WriterKind writerKind;
  private String personIdentifier;
  private String customerIdentifier;
  private List<Grant> grants;
  private Action action;

  public static VersionDao versionOf(
      VersionedDao<?> entityDao,
      SortableIdentifier identifier,
      Object entity,
      Writer writer,
      Action action,
      Instant timestamp) {
    var dao = new VersionDao();
    dao.setPartitionKey(entityDao.getPartitionKey());
    dao.setSortKey(keyPrefixOf(entityDao) + identifier);
    dao.setEntity(treeOf(entity));
    dao.setWriter(writer.name());
    dao.setWriterKind(writer.kind());
    if (writer instanceof User user) {
      dao.setPersonIdentifier(user.personIdentifier());
      dao.setCustomerIdentifier(Objects.toString(user.customerIdentifier(), null));
      dao.setGrants(List.copyOf(user.grants()));
    }
    dao.setAction(action);
    dao.timestamp(timestamp);
    return dao;
  }

  public <T> Version<T> toVersion(Class<T> entityType) {
    return new Version<>(
        identifierOf(getSortKey()),
        entityOf(entity, entityType),
        toWriter(),
        action,
        getCreatedDate());
  }

  public static String keyPrefixOf(VersionedDao<?> entityDao) {
    return VERSION_KEY_PREFIX + entityDao.getSortKey() + KEY_SEPARATOR;
  }

  private static SortableIdentifier identifierOf(String versionSortKey) {
    return new SortableIdentifier(
        versionSortKey.substring(versionSortKey.lastIndexOf(KEY_SEPARATOR) + 1));
  }

  private Writer toWriter() {
    return WriterKind.USER == writerKind
        ? new User(
            writer,
            personIdentifier,
            Optional.ofNullable(customerIdentifier).map(UUID::fromString).orElse(null),
            Set.copyOf(grants))
        : new SystemWriter(writer);
  }

  private static JsonNode treeOf(Object entity) {
    return dtoObjectMapper.valueToTree(entity);
  }

  private static <T> T entityOf(JsonNode entity, Class<T> entityType) {
    return attempt(() -> dtoObjectMapper.treeToValue(entity, entityType))
        .orElseThrow(failure -> new IllegalStateException(ENTITY_NOT_MAPPABLE));
  }

  @DynamoDbConvertedBy(JsonNodeConverter.class)
  public JsonNode getEntity() {
    return entity;
  }

  public void setEntity(JsonNode entity) {
    this.entity = entity;
  }

  public String getWriter() {
    return writer;
  }

  public void setWriter(String writer) {
    this.writer = writer;
  }

  public WriterKind getWriterKind() {
    return writerKind;
  }

  public void setWriterKind(WriterKind writerKind) {
    this.writerKind = writerKind;
  }

  public String getPersonIdentifier() {
    return personIdentifier;
  }

  public void setPersonIdentifier(String personIdentifier) {
    this.personIdentifier = personIdentifier;
  }

  public String getCustomerIdentifier() {
    return customerIdentifier;
  }

  public void setCustomerIdentifier(String customerIdentifier) {
    this.customerIdentifier = customerIdentifier;
  }

  public List<Grant> getGrants() {
    return grants;
  }

  public void setGrants(List<Grant> grants) {
    this.grants = grants;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }
}
