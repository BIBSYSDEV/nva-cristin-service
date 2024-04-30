package no.unit.nva.common;

import static no.unit.nva.common.IdCreatedLogger.CLIENT_CREATED_RESOURCE_TEMPLATE;
import static no.unit.nva.common.IdCreatedLogger.COULD_NOT_EXTRACT_IDENTIFIER_OF_NEWLY_CREATED_RESOURCE;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import no.unit.nva.model.UriId;
import nva.commons.logutils.LogUtils;
import org.junit.jupiter.api.Test;

class IdCreatedLoggerTest {

    @Test
    void shouldNotThrowExceptionWhenParsingOfLogIdFails() {
        final var testAppender = LogUtils.getTestingAppender(IdCreatedLogger.class);
        var mockResource = mock(UriId.class);
        doThrow(RuntimeException.class).when(mockResource).getId();

        try {
            new IdCreatedLogger().logId(mockResource);

            assertThat(testAppender.getMessages(),
                       containsString(COULD_NOT_EXTRACT_IDENTIFIER_OF_NEWLY_CREATED_RESOURCE));
        } catch (Exception ex) {
            fail("Exception was thrown when it should just have continued");
        }
    }

    @Test
    void shouldLogIdWhenSuppliedWithValidData() {
        final var testAppender = LogUtils.getTestingAppender(IdCreatedLogger.class);
        var mockResource = mock(UriId.class);
        var id = randomUri();
        doReturn(id).when(mockResource).getId();

        new IdCreatedLogger().logId(mockResource);

        assertThat(testAppender.getMessages(), containsString(String.format(CLIENT_CREATED_RESOURCE_TEMPLATE, id)));
    }

}