package no.unit.nva.cristin.organization;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("karateTest")
public class OrganizationKarateTestRunner {

    @Karate.Test
    @Tag("karateTest")
    Karate runOrganizationKarateTest() {
        return Karate.run().relativeTo(getClass());
    }

}
