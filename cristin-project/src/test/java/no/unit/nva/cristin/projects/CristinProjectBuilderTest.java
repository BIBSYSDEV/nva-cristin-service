package no.unit.nva.cristin.projects;

import java.util.HashSet;
import java.util.Set;
import no.unit.nva.cristin.projects.model.cristin.CristinContactInfo;
import no.unit.nva.cristin.projects.model.cristin.CristinFundingAmount;
import no.unit.nva.cristin.projects.model.nva.FundingAmount;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import org.junit.jupiter.api.Test;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.NvaProjectBuilderTest.CONTACT_EMAIL;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.IGNORE_LIST;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomNvaProject;
import static no.unit.nva.hamcrest.DoesNotHaveEmptyValues.doesNotHaveEmptyValuesIgnoringFields;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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

    @Test
    void shouldSerializeAndDeserializeCristinContactInfoIntoSameObject() {
        var cristinContactInfo = new CristinContactInfo(randomString(), randomString(), CONTACT_EMAIL, randomString());
        var serialized = attempt(() -> OBJECT_MAPPER.writeValueAsString(cristinContactInfo)).orElseThrow();
        var deserialized =
            attempt(() -> OBJECT_MAPPER.readValue(serialized, CristinContactInfo.class)).orElseThrow();

        assertThat(deserialized, equalTo(cristinContactInfo));
    }

    @Test
    void shouldSerializeAndDeserializeCristinFundingAmountIntoSameObject() {
        var cristinFundingAmount = new CristinFundingAmount(randomString(), randomInteger().doubleValue());
        var serialized = attempt(() -> OBJECT_MAPPER.writeValueAsString(cristinFundingAmount)).orElseThrow();
        var deserialized =
            attempt(() -> OBJECT_MAPPER.readValue(serialized, CristinFundingAmount.class)).orElseThrow();

        assertThat(deserialized, equalTo(cristinFundingAmount));
    }

    private void addFieldsNotSupportedByToCristinProject(NvaProject expected, NvaProject actual) {
        actual.setCreated(expected.getCreated());
        actual.setLastModified(expected.getLastModified());
        actual.setContactInfo(expected.getContactInfo());
        actual.setFundingAmount(expected.getFundingAmount());
        actual.setMethod(expected.getMethod());
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
