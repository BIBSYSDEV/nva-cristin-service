package no.unit.nva.cristin.projects.update;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class UpdateCristinProjectKarateRunnerIT {

    @Karate.Test
    Karate runUpdateCristinProjectKarateTests() {
        return Karate.run().relativeTo(getClass());
    }

}
