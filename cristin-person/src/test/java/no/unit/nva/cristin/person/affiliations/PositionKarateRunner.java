package no.unit.nva.cristin.person.affiliations;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class PositionKarateRunner {

    @Karate.Test
    Karate runKarateFeatures() {
        return Karate.run().relativeTo(getClass());
    }
}
