package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import org.junit.jupiter.api.Test;

import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.IGNORE_LIST;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomNvaProject;
import static no.unit.nva.hamcrest.DoesNotHaveEmptyValues.doesNotHaveEmptyValuesIgnoringFields;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CristinProjectBuilderTest {


    @Test
    void projectShouldBeLossLessConvertedAndEqualAfterConvertedToCristinAndBack() {
        final NvaProject expected = randomNvaProject();
        final CristinProject cristinProject = expected.toCristinProject();
        assertNotNull(cristinProject);
        assertTrue(cristinProject.hasValidContent());
        NvaProject actual = cristinProject.toNvaProject();
        assertThat(actual, doesNotHaveEmptyValuesIgnoringFields(IGNORE_LIST));
        assertEquals(expected, actual);
    }

}
