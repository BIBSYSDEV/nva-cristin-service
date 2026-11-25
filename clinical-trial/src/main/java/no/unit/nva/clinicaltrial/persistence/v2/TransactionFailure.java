package no.unit.nva.clinicaltrial.persistence.v2;

import java.util.List;
import java.util.stream.IntStream;
import software.amazon.awssdk.services.dynamodb.model.CancellationReason;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

/** What a cancelled transaction means for the items it was asked to write. */
record TransactionFailure(TransactionCanceledException cancelled, List<VersionedItem<?, ?>> items) {

  private static final String CONDITIONAL_CHECK_FAILED = "ConditionalCheckFailed";

  static TransactionFailure from(
      TransactionCanceledException cancelled, List<VersionedItem<?, ?>> items) {
    return new TransactionFailure(cancelled, items);
  }

  /**
   * An item written as a create that already exists, or updated from a version it no longer has,
   * failed its condition; an item updated that is not there at all is gone. Cancellations that no
   * condition explains, such as throttling, are rethrown as they came.
   */
  void throwWriteFailure() throws ConditionFailedException, ItemNotFoundException {
    var failedItem = failedItem();
    if (items.get(failedItem).dao().isCreate() || itemExisted(failedItem)) {
      throw new ConditionFailedException();
    }
    throw new ItemNotFoundException();
  }

  private int failedItem() {
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
