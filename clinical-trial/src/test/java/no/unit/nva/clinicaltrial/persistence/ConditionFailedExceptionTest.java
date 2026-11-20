package no.unit.nva.clinicaltrial.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.CancellationReason;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

class ConditionFailedExceptionTest {

  @Test
  void shouldReturnConditionFailedExceptionWhenConditionalCheckFailed() {
    assertThat(
        ConditionFailedException.from(cancelledBecauseOf("ConditionalCheckFailed")),
        instanceOf(ConditionFailedException.class));
  }

  @Test
  void shouldRethrowCancellationThatIsNotConditionFailure() {
    var transactionConflict = cancelledBecauseOf("TransactionConflict");

    assertThrows(
        TransactionCanceledException.class,
        () -> ConditionFailedException.from(transactionConflict));
  }

  private TransactionCanceledException cancelledBecauseOf(String reasonCode) {
    return TransactionCanceledException.builder()
        .cancellationReasons(CancellationReason.builder().code(reasonCode).build())
        .build();
  }
}
