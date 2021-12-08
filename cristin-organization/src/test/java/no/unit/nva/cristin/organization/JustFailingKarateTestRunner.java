package no.unit.nva.cristin.organization;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@Tag("karateTest")
public class JustFailingKarateTestRunner {

    @Test
    void shouldAlwaysFail() {
        fail("JustFailingKarateTestRunner should always fail when running karateTest");
    }

}
