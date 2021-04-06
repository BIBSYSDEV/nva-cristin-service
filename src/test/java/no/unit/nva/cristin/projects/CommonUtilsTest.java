package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.CommonUtils.hasValidContent;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashMap;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import org.junit.jupiter.api.Test;

public class CommonUtilsTest {

    @Test
    void callingHasValidContentOnCristinProjectOnlyReturnsTrueWhenAllRequiredDataArePresent() {
        CristinProject cristinProject = null;
        assertFalse(hasValidContent(cristinProject));

        cristinProject = new CristinProject();
        assertFalse(hasValidContent(cristinProject));

        cristinProject.cristinProjectId = "1234";
        assertFalse(hasValidContent(cristinProject));

        cristinProject.title = new HashMap<>();
        assertFalse(hasValidContent(cristinProject));

        cristinProject.title.put("nb", "Min tittel");
        assertTrue(hasValidContent(cristinProject));
    }
}
