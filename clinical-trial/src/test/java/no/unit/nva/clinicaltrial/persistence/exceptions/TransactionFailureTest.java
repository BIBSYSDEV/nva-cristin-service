package no.unit.nva.clinicaltrial.persistence.exceptions;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.CancellationReason;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

class TransactionFailureTest {

  @Test
  void shouldRethrowCancellationThatNoConditionExplains() {
    var cancelled =
        TransactionCanceledException.builder()
            .cancellationReasons(CancellationReason.builder().code("TransactionConflict").build())
            .build();

    assertThrows(
        TransactionCanceledException.class,
        () -> TransactionFailure.from(cancelled).throwWriteFailure());
  }
}
