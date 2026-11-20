package no.unit.nva.clinicaltrial.persistence;

import static no.unit.nva.clinicaltrial.persistence.DatabaseConstants.CONDITIONAL_CHECK_FAILED;

import software.amazon.awssdk.services.dynamodb.model.CancellationReason;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

public class ConditionFailedException extends Exception {

  private static final String MESSAGE = "Clinical trial does not meet the condition of the write";

  public ConditionFailedException() {
    super(MESSAGE);
  }

  public static ConditionFailedException from(TransactionCanceledException transactionCanceled) {
    if (isConditionFailure(transactionCanceled)) {
      return new ConditionFailedException();
    }
    throw transactionCanceled;
  }

  private static boolean isConditionFailure(TransactionCanceledException transactionCanceled) {
    return transactionCanceled.cancellationReasons().stream()
        .map(CancellationReason::code)
        .anyMatch(CONDITIONAL_CHECK_FAILED::equals);
  }
}
