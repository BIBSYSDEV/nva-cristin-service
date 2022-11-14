package no.unit.nva.cristin.projects.create;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class CreateCristinProjectKarateRunnerIT {

    @Karate.Test
    Karate runCreateCristinProjectKarateTests() {
        return Karate.run().relativeTo(getClass());
    }

}
