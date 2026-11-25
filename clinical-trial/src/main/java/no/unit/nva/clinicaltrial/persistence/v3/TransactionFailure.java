package no.unit.nva.clinicaltrial.persistence.v3;

import java.util.List;
import java.util.stream.IntStream;
import software.amazon.awssdk.services.dynamodb.model.CancellationReason;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

/** What a cancelled transaction means for the rows it was asked to write. */
record TransactionFailure(TransactionCanceledException cancelled, List<VersionedDao<?>> rows) {

  private static final String CONDITIONAL_CHECK_FAILED = "ConditionalCheckFailed";

  static TransactionFailure from(
      TransactionCanceledException cancelled, List<VersionedDao<?>> rows) {
    return new TransactionFailure(cancelled, rows);
  }

  /**
   * A row written as a create that already exists, or updated from a version it no longer has,
   * failed its condition; a row updated that is not there at all is gone. Cancellations that no
   * condition explains, such as throttling, are rethrown as they came.
   */
  void throwWriteFailure() throws ConditionFailedException, ItemNotFoundException {
    var failed = failedRow();
    if (rows.get(failed).isCreate() || itemExisted(failed)) {
      throw new ConditionFailedException();
    }
    throw new ItemNotFoundException();
  }

  private int failedRow() {
    return IntStream.range(0, reasons().size())
        .filter(index -> CONDITIONAL_CHECK_FAILED.equals(reasons().get(index).code()))
        .findFirst()
        .orElseThrow(() -> cancelled);
  }

  private boolean itemExisted(int index) {
    return reasons().get(index).hasItem();
  }

  private List<CancellationReason> reasons() {
    return cancelled.cancellationReasons();
  }
}
