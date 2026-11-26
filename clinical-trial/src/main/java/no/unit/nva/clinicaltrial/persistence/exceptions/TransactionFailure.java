package no.unit.nva.clinicaltrial.persistence.exceptions;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import no.unit.nva.clinicaltrial.persistence.dao.Dao;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.CancellationReason;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

public record TransactionFailure(TransactionCanceledException cancelled) {

  private static final String CONDITIONAL_CHECK_FAILED = "ConditionalCheckFailed";

  public static TransactionFailure from(TransactionCanceledException cancelled) {
    return new TransactionFailure(cancelled);
  }

  public void throwWriteFailure() throws ConditionFailedException, ItemNotFoundException {
    var reason = conditionFailure();
    if (hasItem(reason)) {
      throw new ConditionFailedException(storedVersionOf(reason.item()));
    }
    throw new ItemNotFoundException();
  }

  private CancellationReason conditionFailure() {
    return cancelled.cancellationReasons().stream()
        .filter(reason -> CONDITIONAL_CHECK_FAILED.equals(reason.code()))
        .findFirst()
        .orElseThrow(() -> cancelled);
  }

  private static boolean hasItem(CancellationReason reason) {
    return Objects.nonNull(reason.item()) && !reason.item().isEmpty();
  }

  private static Long storedVersionOf(Map<String, AttributeValue> item) {
    return Optional.ofNullable(item.get(Dao.VERSION_ATTRIBUTE))
        .map(AttributeValue::n)
        .map(Long::valueOf)
        .orElse(null);
  }
}
