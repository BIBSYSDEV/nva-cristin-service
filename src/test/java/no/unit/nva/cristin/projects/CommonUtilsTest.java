package no.unit.nva.cristin.projects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashMap;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import org.junit.jupiter.api.Test;

public class CommonUtilsTest {

    @Test
    void callingHasValidContentOnCristinProjectOnlyReturnsTrueWhenAllRequiredDataArePresent() {
        CristinProject cristinProject = new CristinProject();
        assertFalse(cristinProject.hasValidContent());

        cristinProject.setCristinProjectId("1234");
        assertFalse(cristinProject.hasValidContent());

        cristinProject.setTitle(new HashMap<>());
        assertFalse(cristinProject.hasValidContent());

        cristinProject.getTitle().put("nb", "Min tittel");
        assertTrue(cristinProject.hasValidContent());
    }
}
