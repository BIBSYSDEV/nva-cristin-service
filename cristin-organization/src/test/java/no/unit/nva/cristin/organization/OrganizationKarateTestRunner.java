package no.unit.nva.cristin.organization;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@Tag("karateTest")
public class OrganizationKarateTestRunner {

    @Test
    void runOrganizationKarateTest() {
        Karate.run("organization").relativeTo(getClass()).outputCucumberJson(true);
    }


}
