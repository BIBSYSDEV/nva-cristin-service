package no.unit.nva.utils;

import static no.unit.nva.utils.VersioningUtils.extractVersion;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

class VersioningUtilsTest {

    public static final String ACCEPT_HEADER_VALUE = "application/json; version=2023-05-10";
    public static final String ACCEPT_HEADER_VALUE_WITHOUT_VERSION = "application/json";
    public static final String VERSION_VALUE = "2023-05-10";

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

}
