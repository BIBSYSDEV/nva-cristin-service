package no.unit.nva.clinicaltrial.persistence.v1;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;

import com.fasterxml.jackson.databind.JavaType;
import java.time.Instant;
import java.util.Map;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/** A row of the table, mapped to and from its attributes by Jackson. */
public interface Dao {

  String ITEM_NOT_MAPPABLE = "Item is not mappable";

  String partitionKey();

  String sortKey();

  Instant createdDate();

  Instant modifiedDate();

  default Map<String, AttributeValue> toItem() {
    var json =
        attempt(() -> dtoObjectMapper.writeValueAsString(this))
            .orElseThrow(failure -> new IllegalStateException(ITEM_NOT_MAPPABLE));
    return EnhancedDocument.fromJson(json).toMap();
  }

  static <D> D fromItem(Map<String, AttributeValue> item, Class<D> daoType) {
    return fromItem(item, dtoObjectMapper.getTypeFactory().constructType(daoType));
  }

  static <D> D fromItem(Map<String, AttributeValue> item, Class<?> daoType, Class<?> entityType) {
    return fromItem(
        item, dtoObjectMapper.getTypeFactory().constructParametricType(daoType, entityType));
  }

  private static <D> D fromItem(Map<String, AttributeValue> item, JavaType daoType) {
    var json = EnhancedDocument.fromAttributeValueMap(item).toJson();
    return attempt(() -> dtoObjectMapper.<D>readValue(json, daoType))
        .orElseThrow(failure -> new IllegalStateException(ITEM_NOT_MAPPABLE));
  }
}
