package no.unit.nva.cristin.projects;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("karateTest")
class ProjectKarateTestRunner {

    @Test
    Karate testProject() {
        return Karate.run("project").relativeTo(getClass()).outputCucumberJson(true);

    }
}
