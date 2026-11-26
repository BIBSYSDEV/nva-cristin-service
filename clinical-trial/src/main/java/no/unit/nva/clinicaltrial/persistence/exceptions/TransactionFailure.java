package no.unit.nva.clinicaltrial.persistence.exceptions;

import java.util.List;
import software.amazon.awssdk.services.dynamodb.model.CancellationReason;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

public record TransactionFailure(TransactionCanceledException cancelled) {

  private static final String CONDITIONAL_CHECK_FAILED = "ConditionalCheckFailed";

  public static TransactionFailure from(TransactionCanceledException cancelled) {
    return new TransactionFailure(cancelled);
  }

  public void throwWriteFailure() throws ConditionFailedException, ItemNotFoundException {
    if (conditionFailure().hasItem()) {
      throw new ConditionFailedException();
    }
    throw new ItemNotFoundException();
  }

  private CancellationReason conditionFailure() {
    return reasons().stream()
        .filter(reason -> CONDITIONAL_CHECK_FAILED.equals(reason.code()))
        .findFirst()
        .orElseThrow(() -> cancelled);
  }

  private List<CancellationReason> reasons() {
    return cancelled.cancellationReasons();
  }
}
