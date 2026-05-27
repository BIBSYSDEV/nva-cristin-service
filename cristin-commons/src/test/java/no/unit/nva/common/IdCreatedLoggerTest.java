package no.unit.nva.common;

import static no.unit.nva.common.IdCreatedLogger.CLIENT_CREATED_RESOURCE_TEMPLATE;
import static no.unit.nva.common.IdCreatedLogger.COULD_NOT_EXTRACT_IDENTIFIER_OF_NEWLY_CREATED_RESOURCE;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import no.unit.nva.model.UriId;
import nva.commons.logutils.LogRecorder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class IdCreatedLoggerTest {

  @Test
  void shouldNotThrowExceptionWhenParsingOfLogIdFails() {
    final var logRecorder = LogRecorder.forClass(IdCreatedLogger.class);
    var mockResource = mock(UriId.class);
    doThrow(RuntimeException.class).when(mockResource).getId();

    try {
      new IdCreatedLogger().logId(mockResource);

      Assertions.assertThat(logRecorder.messages())
          .contains(COULD_NOT_EXTRACT_IDENTIFIER_OF_NEWLY_CREATED_RESOURCE);
    } catch (Exception ex) {
      fail("Exception was thrown when it should just have continued");
    }
  }

  @Test
  void shouldLogIdWhenSuppliedWithValidData() {
    final var logRecorder = LogRecorder.forClass(IdCreatedLogger.class);
    var mockResource = mock(UriId.class);
    var id = randomUri();
    doReturn(id).when(mockResource).getId();

    new IdCreatedLogger().logId(mockResource);

    Assertions.assertThat(logRecorder.messages())
        .contains(String.format(CLIENT_CREATED_RESOURCE_TEMPLATE, id));
  }
}
