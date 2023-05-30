package no.unit.nva.utils;

import static no.unit.nva.utils.VersioningUtils.extractVersion;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

class VersioningUtilsTest {

    public static final String ACCEPT_HEADER_VALUE = "application/json; version=2023-05-10";
    public static final String ACCEPT_HEADER_VALUE_WITH_QUOTES = "application/json; version=\"2023-05-10\"";
    public static final String ACCEPT_HEADER_VALUE_WITHOUT_VERSION = "application/json";
    public static final String VERSION_VALUE = "2023-05-10";
    public static final String INVALID_HEADER_VALUE = "someHeaderValue";

    @Test
    void shouldExtractVersionFromHeader() {
        var actual = extractVersion(ACCEPT_HEADER_VALUE);

        assertThat(actual, equalTo(VERSION_VALUE));
    }

    @Test
    void shouldReturnNullWhenVersionNotPresent() {
        var actual = extractVersion(ACCEPT_HEADER_VALUE_WITHOUT_VERSION);

        assertThat(actual, equalTo(null));
    }

    @Test
    void shouldExtractVersionFromHeaderWhenSurroundedWithQuotes() {
        var actual = extractVersion(ACCEPT_HEADER_VALUE_WITH_QUOTES);

        assertThat(actual, equalTo(VERSION_VALUE));
    }

    @Test
    void shouldReturnNullWhenHeaderIsIncorrectlyFormatted() {
        var actual = extractVersion(INVALID_HEADER_VALUE);

        assertThat(actual, equalTo(null));
    }

}
