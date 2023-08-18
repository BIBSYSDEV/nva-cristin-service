package no.unit.nva.cristin.projects.model.cristin;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.IGNORE_LIST;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomNvaProject;
import static no.unit.nva.cristin.projects.model.nva.Funding.UNCONFIRMED_FUNDING;
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
    public static final String URI_ENCODED_FUNDING_SOURCE_URI = "https://api.dev.nva.aws.unit"
                                                                + ".no/cristin/funding-sources/EC%2FFP7";

    @Test
    void projectShouldBeLossLessConvertedAndEqualAfterConvertedToCristinAndBack() {
        final var expected = randomNvaProject();
        final var cristinProject = expected.toCristinProject();

        assertNotNull(cristinProject);
        assertTrue(cristinProject.hasValidContent());

        var actual = cristinProject.toNvaProject();
        addFieldsNotSupportedByToCristinProject(expected, actual);

        assertThat(actual, doesNotHaveEmptyValuesIgnoringFields(IGNORE_LIST));
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

    @ParameterizedTest
    @CsvSource({"NFR,NFR", "EC%2FFP7,EC/FP7", URI_ENCODED_FUNDING_SOURCE_URI + ",EC/FP7"})
    void shouldParseAndDecodeDifferentValuesForFundingSources(String input, String expected) {
        var nvaProject = randomNvaProject();
        var funding = new Funding(UNCONFIRMED_FUNDING, URI.create(input), null, null);
        nvaProject.setFunding(List.of(funding));
        var cristinProject = new CristinProjectBuilder(nvaProject).build();
        var cristinFunding = cristinProject.getProjectFundingSources().get(0);
        var actual = cristinFunding.getFundingSourceCode();

        assertThat(actual, equalTo(expected));
    }

    private void addFieldsNotSupportedByToCristinProject(NvaProject expected, NvaProject actual) {
        actual.setCreated(expected.getCreated());
        actual.setLastModified(expected.getLastModified());
        actual.setFundingAmount(expected.getFundingAmount());
    }

}
