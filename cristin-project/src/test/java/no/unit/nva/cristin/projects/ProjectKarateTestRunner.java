package no.unit.nva.cristin.projects;

import com.intuit.karate.junit5.Karate;

class ProjectKarateTestRunner {

    @Karate.Test
    Karate testProject() {
        return Karate.run("project").relativeTo(getClass()).outputCucumberJson(true);

    }
}
