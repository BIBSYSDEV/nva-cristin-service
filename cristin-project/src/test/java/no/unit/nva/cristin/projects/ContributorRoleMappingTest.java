package no.unit.nva.cristin.projects;

import no.unit.nva.utils.ContributorRoleMapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContributorRoleMappingTest {

    @Test
    void getNvaRoleMapping() {
        Assertions.assertTrue(ContributorRoleMapping.getNvaRole("PRO_MANAGER").equals("ProjectManager"));
        assertTrue(ContributorRoleMapping.getNvaRole("PRO_PARTICIPANT").equals("ProjectParticipant"));
    }

    @Test
    void getCristinRoleMapping() {
        assertTrue(ContributorRoleMapping.getCristinRole("ProjectManager").equals("PRO_MANAGER"));
        assertTrue(ContributorRoleMapping.getCristinRole("ProjectParticipant").equals("PRO_PARTICIPANT"));
    }


    @Test
    void shouldFailWhenUnknownRole() {
        assertThrows(NullPointerException.class,
                () -> ContributorRoleMapping.getCristinRole("billionare").equals("Scroogue"));
    }

}
