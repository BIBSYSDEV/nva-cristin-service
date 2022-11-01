package no.unit.nva.cristin.projects;

import java.util.HashSet;
import java.util.Set;
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
        final var expected = randomNvaProject();
        final var cristinProject = expected.toCristinProject();

        assertNotNull(cristinProject);
        assertTrue(cristinProject.hasValidContent());

        var actual = cristinProject.toNvaProject();
        var ignored = addFieldsToIgnoreListNotSupportedByCristinPost();
        addFieldsNotSupportedByToCristinProject(expected, actual);

        assertThat(actual, doesNotHaveEmptyValuesIgnoringFields(ignored));
        assertEquals(expected, actual);
    }

    private void addFieldsNotSupportedByToCristinProject(NvaProject expected, NvaProject actual) {
        actual.setCreated(expected.getCreated());
        actual.setLastModified(expected.getLastModified());
        actual.setContactInfo(expected.getContactInfo());
    }

    private Set<String> addFieldsToIgnoreListNotSupportedByCristinPost() {
        var ignoreList = new HashSet<>(IGNORE_LIST);

        ignoreList.add(".created.sourceShortName");
        ignoreList.add(".created.date");
        ignoreList.add(".lastModified.sourceShortName");
        ignoreList.add(".lastModified.date");
        ignoreList.add("lastModified");
        ignoreList.add("created");

        return ignoreList;
    }
}
