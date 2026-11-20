package no.unit.nva.clinicaltrial.persistence;

import static no.unit.nva.clinicaltrial.persistence.DatabaseConstants.ITEM_NOT_READABLE;
import static no.unit.nva.clinicaltrial.persistence.DatabaseConstants.ITEM_NOT_SERIALIZABLE;
import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;

import com.fasterxml.jackson.databind.JavaType;
import java.time.Instant;
import java.util.Map;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public interface Dao {

  String partitionKey();

  String sortKey();

  Instant createdDate();

  Instant modifiedDate();

  static <D> D fromItem(Map<String, AttributeValue> item, Class<D> daoType) {
    return fromItem(item, dtoObjectMapper.getTypeFactory().constructType(daoType));
  }

  static <D> D fromItem(Map<String, AttributeValue> item, Class<?> daoType, Class<?> entityType) {
    return fromItem(
        item, dtoObjectMapper.getTypeFactory().constructParametricType(daoType, entityType));
  }

  default Map<String, AttributeValue> toItem() {
    var json =
        attempt(() -> dtoObjectMapper.writeValueAsString(this))
            .orElseThrow(failure ->
                new IllegalStateException(ITEM_NOT_SERIALIZABLE, failure.getException()));
    return EnhancedDocument.fromJson(json).toMap();
  }

  private static <D> D fromItem(Map<String, AttributeValue> item, JavaType daoType) {
    var json = EnhancedDocument.fromAttributeValueMap(item).toJson();
    return attempt(() -> dtoObjectMapper.<D>readValue(json, daoType))
        .orElseThrow(
            failure -> new IllegalStateException(ITEM_NOT_READABLE, failure.getException()));
  }
}
