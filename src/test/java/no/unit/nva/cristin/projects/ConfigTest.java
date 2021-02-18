package no.unit.nva.cristin.projects;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ConfigTest {

    @Test
    public void testCorsHeaderNotSet() {
        final Config config = Config.getInstance();
        config.setCorsHeader(null);
        final String corsHeader = config.getCorsHeader();
        assertNull(corsHeader);
    }

}
