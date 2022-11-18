package no.unit.nva.cristin.projects.fetch;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class FetchCristinProjectKarateRunnerIT {

    @Karate.Test
    Karate runFetchCristinProjectKarateTests() {
        return Karate.run().relativeTo(getClass());
    }

}
