package no.unit.nva.cristin.projects;

import no.unit.nva.utils.ContributorRoleMapping;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ContributorRoleMappingTest {

    @Test
    void shouldFailWhenUnknownRole() {
        assertThrows(NoSuchElementException.class,
                () -> ContributorRoleMapping.getCristinRole("billionare").get().equals("Scroogue"));
    }
}
