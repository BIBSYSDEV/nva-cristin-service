package no.unit.nva.cristin.projects.query;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class QueryCristinProjectKarateRunnerIT {

    // Also runs tests in subdirectories
    @Karate.Test
    Karate runQueryCristinProjectKarateTests() {
        return Karate.run().relativeTo(getClass());
    }

}
