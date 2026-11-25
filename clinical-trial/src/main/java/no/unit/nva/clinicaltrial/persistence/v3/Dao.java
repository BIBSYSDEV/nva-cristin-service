package no.unit.nva.clinicaltrial.persistence.v3;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;

import com.fasterxml.jackson.databind.JavaType;
import java.time.Instant;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;

/** A row of the table, mapped to and from a document by Jackson. */
public interface Dao {

  String ITEM_NOT_MAPPABLE = "Item is not mappable";

  String partitionKey();

  String sortKey();

  Instant createdDate();

  Instant modifiedDate();

  default EnhancedDocument toDocument() {
    var json =
        attempt(() -> dtoObjectMapper.writeValueAsString(this))
            .orElseThrow(failure -> new IllegalStateException(ITEM_NOT_MAPPABLE));
    return EnhancedDocument.fromJson(json);
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
        .orElseThrow(failure -> new IllegalStateException(ITEM_NOT_MAPPABLE));
  }
}
