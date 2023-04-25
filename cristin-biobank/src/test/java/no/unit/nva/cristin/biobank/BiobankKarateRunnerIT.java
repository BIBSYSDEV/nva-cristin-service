package no.unit.nva.cristin.biobank;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class BiobankKarateRunnerIT {

    @Karate.Test
    Karate runBiobankKarateTests() {
        return Karate.run().relativeTo(getClass());
    }

}
