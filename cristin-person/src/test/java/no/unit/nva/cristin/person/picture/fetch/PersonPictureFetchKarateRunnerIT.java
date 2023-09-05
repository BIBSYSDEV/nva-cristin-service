package no.unit.nva.cristin.person.picture.fetch;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class PersonPictureFetchKarateRunnerIT {

    @Karate.Test
    Karate runKarateFeatures() {
        return Karate.run().relativeTo(getClass());
    }

}
