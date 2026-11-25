package no.unit.nva.clinicaltrial.persistence.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Entity;
import no.unit.nva.clinicaltrial.model.Period;
import no.unit.nva.clinicaltrial.model.Submission;
import no.unit.nva.clinicaltrial.model.write.Writer;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactUpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ReturnValuesOnConditionCheckFailure;

public interface VersionedDao<E extends Entity> extends Dao {

  long FIRST_VERSION = 1L;
  String VERSION_ATTRIBUTE = "version";
  String EXPECTED_VERSION = ":expectedVersion";
  String CREATE_CONDITION = "attribute_not_exists(" + PARTITION_KEY + ")";
  String UPDATE_CONDITION = VERSION_ATTRIBUTE + " = " + EXPECTED_VERSION;

  static VersionedDao<?> fromEntity(Entity entity, Instant timestamp) {
    return switch (entity) {
      case ClinicalTrial trial -> ClinicalTrialDao.from(trial, timestamp);
      case Period period -> PeriodDao.from(period, timestamp);
      case Submission submission -> SubmissionDao.from(submission, timestamp);
    };
  }

  Long version();

  E entityAtVersion(Long version);

  default E toEntity() {
    return entityAtVersion(version());
  }

  default TransactUpdateItemEnhancedRequest<EnhancedDocument> toUpdateRequest() {
    return TransactUpdateItemEnhancedRequest.builder(EnhancedDocument.class)
        .item(toDocument().toBuilder().putNumber(VERSION_ATTRIBUTE, nextVersion()).build())
        .conditionExpression(condition())
        .returnValuesOnConditionCheckFailure(ReturnValuesOnConditionCheckFailure.ALL_OLD)
        .build();
  }

  private Expression condition() {
    return isCreate()
        ? Expression.builder().expression(CREATE_CONDITION).build()
        : Expression.builder()
            .expression(UPDATE_CONDITION)
            .expressionValues(
                Map.of(EXPECTED_VERSION, AttributeValue.fromN(Long.toString(version()))))
            .build();
  }

  default VersionDao<E> toVersionDao(Writer writer) {
    return VersionDao.versionOf(this, writer);
  }

  @JsonIgnore
  default boolean isCreate() {
    return Objects.isNull(version());
  }

  default long nextVersion() {
    return isCreate() ? FIRST_VERSION : version() + 1;
  }
}
