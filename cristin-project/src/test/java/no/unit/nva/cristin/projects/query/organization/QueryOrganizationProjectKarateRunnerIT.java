package no.unit.nva.cristin.projects.query.organization;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class QueryOrganizationProjectKarateRunnerIT {

    @Karate.Test
    Karate runQueryOrganizationProjectKarateTests() {
        return Karate.run().relativeTo(getClass());
    }

}
