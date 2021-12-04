package no.unit.nva.cristin.organization;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("karateTest")
class EnvironmentDumpKarateTestRunner {

    @Test
    Karate testEnvironment() {
        return Karate.run("environment").relativeTo(getClass()).outputCucumberJson(true);
    }
}
