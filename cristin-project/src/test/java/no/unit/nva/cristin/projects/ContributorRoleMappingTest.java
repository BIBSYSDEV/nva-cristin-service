package no.unit.nva.cristin.projects;

import no.unit.nva.utils.ContributorRoleMapping;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContributorRoleMappingTest {

    @Test
    void shouldReturnManagerMapping() {
        assertTrue(ContributorRoleMapping.getNvaRole("PRO_MANAGER").get().equals("ProjectManager"));
        assertTrue(ContributorRoleMapping.getCristinRole("ProjectManager").get().equals("PRO_MANAGER"));
    }

    @Test
    void shouldReturnParticipantMapping() {
        assertTrue(ContributorRoleMapping.getNvaRole("PRO_PARTICIPANT").get().equals("ProjectParticipant"));
        assertTrue(ContributorRoleMapping.getCristinRole("ProjectParticipant").get().equals("PRO_PARTICIPANT"));
    }

    @Test
    void shouldFailWhenUnknownRole() {
        assertThrows(NoSuchElementException.class,
                () -> ContributorRoleMapping.getCristinRole("billionare").get().equals("Scroogue"));
    }

}
