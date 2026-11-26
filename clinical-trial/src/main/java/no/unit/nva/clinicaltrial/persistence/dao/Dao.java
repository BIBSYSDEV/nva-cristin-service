package no.unit.nva.clinicaltrial.persistence.dao;

import static no.unit.nva.clinicaltrial.persistence.dao.TableAttributes.KEY_SEPARATOR;
import static no.unit.nva.clinicaltrial.persistence.dao.TableAttributes.PARTITION_KEY;
import static no.unit.nva.clinicaltrial.persistence.dao.TableAttributes.SORT_KEY;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.JavaType;
import java.time.Instant;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.TableMetadata;
import software.amazon.awssdk.enhanced.dynamodb.document.DocumentTableSchema;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;

@JsonTypeInfo(use = Id.NAME, property = TableAttributes.TYPE_ATTRIBUTE)
public interface Dao {

  String PROJECT_KEY_PREFIX = "PROJECT" + KEY_SEPARATOR;
  String ITEM_NOT_MAPPABLE = "Item is not mappable";

  DocumentTableSchema SCHEMA =
      DocumentTableSchema.builder()
          .addIndexPartitionKey(
              TableMetadata.primaryIndexName(), PARTITION_KEY, AttributeValueType.S)
          .addIndexSortKey(TableMetadata.primaryIndexName(), SORT_KEY, AttributeValueType.S)
          .build();

  String partitionKey();

  String sortKey();

  Instant modifiedDate();

  default EnhancedDocument toDocument() {
    var json =
        attempt(() -> dtoObjectMapper.writeValueAsString(this))
            .orElseThrow(
                failure -> new IllegalStateException(ITEM_NOT_MAPPABLE, failure.getException()));
    return EnhancedDocument.fromJson(json);
  }

  static String projectKeyOf(String projectIdentifier) {
    return PROJECT_KEY_PREFIX + projectIdentifier;
  }

  static String projectIdentifierOf(String partitionKey) {
    return partitionKey.substring(PROJECT_KEY_PREFIX.length());
  }

  static <D> D fromDocument(EnhancedDocument document, Class<D> daoType) {
    return fromDocument(document, dtoObjectMapper.getTypeFactory().constructType(daoType));
  }

  static <D> D fromDocument(EnhancedDocument document, Class<?> daoType, Class<?> entityType) {
    return fromDocument(
        document, dtoObjectMapper.getTypeFactory().constructParametricType(daoType, entityType));
  }

  private static <D> D fromDocument(EnhancedDocument document, JavaType daoType) {
    return attempt(() -> dtoObjectMapper.<D>readValue(document.toJson(), daoType))
        .orElseThrow(
            failure -> new IllegalStateException(ITEM_NOT_MAPPABLE, failure.getException()));
  }
}
