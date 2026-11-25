package no.unit.nva.clinicaltrial.persistence.v2;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static nva.commons.core.attempt.Try.attempt;

import com.fasterxml.jackson.databind.JsonNode;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class JsonNodeConverter implements AttributeConverter<JsonNode> {

  private static final String ENTITY_NOT_READABLE = "Entity is not readable";

  @Override
  public AttributeValue transformFrom(JsonNode entity) {
    return AttributeValue.fromM(EnhancedDocument.fromJson(entity.toString()).toMap());
  }

  @Override
  public JsonNode transformTo(AttributeValue attributeValue) {
    var json = EnhancedDocument.fromAttributeValueMap(attributeValue.m()).toJson();
    return attempt(() -> dtoObjectMapper.readTree(json))
        .orElseThrow(failure -> new IllegalStateException(ENTITY_NOT_READABLE));
  }

  @Override
  public EnhancedType<JsonNode> type() {
    return EnhancedType.of(JsonNode.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.M;
  }
}
