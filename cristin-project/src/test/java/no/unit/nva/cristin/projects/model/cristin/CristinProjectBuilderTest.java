package no.unit.nva.cristin.projects.model.cristin;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import org.junit.jupiter.api.Test;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.IGNORE_LIST;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomNvaProject;
import static no.unit.nva.hamcrest.DoesNotHaveEmptyValues.doesNotHaveEmptyValuesIgnoringFields;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.ioutils.IoUtils.stringFromResources;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CristinProjectBuilderTest {

    public static final String ONE_CRISTIN_PROJECT = "cristinGetProjectResponse.json";

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

    @Test
    void shouldSerializeAndDeserializeCristinProjectIntoSameObject() {
        var cristinJson = stringFromResources(Path.of(ONE_CRISTIN_PROJECT));
        var cristinProject =
            attempt(() -> OBJECT_MAPPER.readValue(cristinJson, CristinProject.class)).get();
        var serialized = attempt(() -> OBJECT_MAPPER.writeValueAsString(cristinProject)).orElseThrow();
        var deserialized = attempt(() -> OBJECT_MAPPER.readValue(serialized, CristinProject.class)).get();

        assertThat(deserialized, equalTo(cristinProject));
    }

    // TODO: Remove fields from this method when they are POSTable to Cristin
    private void addFieldsNotSupportedByToCristinProject(NvaProject expected, NvaProject actual) {
        actual.setCreated(expected.getCreated());
        actual.setLastModified(expected.getLastModified());
        actual.setContactInfo(expected.getContactInfo());
        actual.setFundingAmount(expected.getFundingAmount());
        actual.setProjectCategories(expected.getProjectCategories());
        actual.setKeywords(expected.getKeywords());
        actual.setExternalSources(expected.getExternalSources());
        actual.setRelatedProjects(expected.getRelatedProjects());
        actual.setHealthProjectData(expected.getHealthProjectData());
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
