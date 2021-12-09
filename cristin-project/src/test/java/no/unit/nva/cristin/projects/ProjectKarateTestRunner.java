package no.unit.nva.cristin.projects;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
class ProjectKarateTestRunner {

    @Karate.Test
    Karate runAllProjectKarateTests() {
        return Karate.run().relativeTo(getClass());
    }
}
