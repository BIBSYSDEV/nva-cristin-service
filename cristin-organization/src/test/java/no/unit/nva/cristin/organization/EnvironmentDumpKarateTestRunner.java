package no.unit.nva.cristin.organization;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("karateTest")
class EnvironmentDumpKarateTestRunner {

    @Tag("karateTest")
    @Test
    void testEnvironment() {
        Karate.run("environment").relativeTo(getClass()).outputCucumberJson(true);
        System.out.println("KarateTests run");
    }
}
