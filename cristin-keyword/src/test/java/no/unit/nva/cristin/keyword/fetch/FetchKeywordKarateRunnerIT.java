package no.unit.nva.cristin.keyword.fetch;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class FetchKeywordKarateRunnerIT {

    @Karate.Test
    Karate runKarateFeatures() {
        return Karate.run().relativeTo(getClass());
    }

}
