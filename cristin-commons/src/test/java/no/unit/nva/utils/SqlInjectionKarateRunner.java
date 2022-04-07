package no.unit.nva.utils;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class SqlInjectionKarateRunner {

    @Karate.Test
    Karate runKarateFeatures() {
        return Karate.run().relativeTo(getClass());
    }

}
