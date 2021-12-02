package no.unit.nva.cristin.organization;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
class OrganizationKarateTestRunner {

    @Karate.Test
    Karate testEnvironment() {
        System.out.println("Running karate tests in OrganizationKarateTestRunner");
        return Karate.run("environment").relativeTo(getClass()).outputCucumberJson(true);

    }
}
