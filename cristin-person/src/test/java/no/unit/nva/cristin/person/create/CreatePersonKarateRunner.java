package no.unit.nva.cristin.person.create;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class CreatePersonKarateRunner {

    @Tag("karateTest")
    @Karate.Test
    Karate runKarateFeatures() {
        return Karate.run().relativeTo(getClass());
    }
}
