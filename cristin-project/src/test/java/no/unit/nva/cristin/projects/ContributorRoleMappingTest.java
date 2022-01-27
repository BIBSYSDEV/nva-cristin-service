package no.unit.nva.cristin.projects;

import no.unit.nva.utils.ContributorRoleMapping;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContributorRoleMappingTest {

    @Test
    void shouldReturnManagerMapping() {
        assertTrue(ContributorRoleMapping.getNvaRole("PRO_MANAGER").equals("ProjectManager"));
        assertTrue(ContributorRoleMapping.getCristinRole("ProjectManager").equals("PRO_MANAGER"));
    }

    @Test
    void shouldReturnParticipantMapping() {
        assertTrue(ContributorRoleMapping.getNvaRole("PRO_PARTICIPANT").equals("ProjectParticipant"));
        assertTrue(ContributorRoleMapping.getCristinRole("ProjectParticipant").equals("PRO_PARTICIPANT"));
    }

    @Test
    void shouldFailWhenUnknownRole() {
        assertThrows(NullPointerException.class,
                () -> ContributorRoleMapping.getCristinRole("billionare").equals("Scroogue"));
    }

}
