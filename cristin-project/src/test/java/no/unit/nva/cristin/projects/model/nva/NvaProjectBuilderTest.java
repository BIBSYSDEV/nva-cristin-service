package no.unit.nva.cristin.projects.model.nva;

import java.util.List;
import no.unit.nva.cristin.projects.model.cristin.CristinFundingSource;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static no.unit.nva.cristin.projects.model.nva.NvaProject.PROJECT_CONTEXT;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.ioutils.IoUtils.stringFromResources;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NvaProjectBuilderTest {

    private static final String API_RESPONSE_ONE_NVA_PROJECT_JSON =
        "nvaApiGetResponseOneNvaProject.json";
    private static final String cristinGetProject = stringFromResources(Path.of("cristinGetProjectResponse.json"));
    public static final String URI_ENCODED_FUNDING_SOURCE_URI = "https://api.dev.nva.aws.unit"
                                                                + ".no/cristin/funding-sources/EC%2FFP7";
    public static final String FUNDING_SOURCE_CODE = "EC/FP7";

    @Test
    void shouldReturnNvaProjectWhenCallingNvaProjectBuilderMethodWithValidCristinProject() throws Exception {
        String expected = stringFromResources(
                Path.of(API_RESPONSE_ONE_NVA_PROJECT_JSON));
        CristinProject cristinProject =
            attempt(() -> OBJECT_MAPPER.readValue(cristinGetProject, CristinProject.class)).get();
        NvaProject nvaProject = new NvaProjectBuilder(cristinProject).build();
        nvaProject.setContext(PROJECT_LOOKUP_CONTEXT_URL);
        String actual = attempt(() -> OBJECT_MAPPER.writeValueAsString(nvaProject)).get();

        assertEquals(OBJECT_MAPPER.readTree(expected), OBJECT_MAPPER.readTree(actual));
    }

    @Test
    void shouldMapAllSupportedFieldsFoundInCristinJsonToCorrectNvaJson() throws Exception {
        var cristinProject =
            attempt(() -> OBJECT_MAPPER.readValue(cristinGetProject, CristinProject.class)).get();
        var nvaProject = new NvaProjectBuilder(cristinProject).build();
        nvaProject.setContext(PROJECT_CONTEXT);
        var expected = stringFromResources(Path.of(API_RESPONSE_ONE_NVA_PROJECT_JSON));
        var actual = OBJECT_MAPPER.valueToTree(nvaProject);

        assertThat(actual, equalTo(OBJECT_MAPPER.readTree(expected)));
    }

    @Test
    void shouldSerializeAndDeserializeNvaProjectIntoSameObject() {
        var nvaJson =
            stringFromResources(Path.of(API_RESPONSE_ONE_NVA_PROJECT_JSON));
        var nvaProject =
            attempt(() -> OBJECT_MAPPER.readValue(nvaJson, NvaProject.class)).get();
        var serialized = attempt(() -> OBJECT_MAPPER.writeValueAsString(nvaProject)).orElseThrow();
        var deserialized = attempt(() -> OBJECT_MAPPER.readValue(serialized, NvaProject.class)).get();

        assertThat(deserialized, equalTo(nvaProject));
    }

    @Test
    void shouldUrlEncodeFundingSourceWithEntities() {
        var cristinFunding = new CristinFundingSource();
        cristinFunding.setFundingSourceCode(FUNDING_SOURCE_CODE);
        var cristinProject = new CristinProject();
        addRequiredFields(cristinProject);
        cristinProject.setProjectFundingSources(List.of(cristinFunding));
        var nvaProject = new NvaProjectBuilder(cristinProject).build();

        var actual = nvaProject.getFunding().get(0).getSource().toString();

        assertThat(actual, equalTo(URI_ENCODED_FUNDING_SOURCE_URI));
    }

    private void addRequiredFields(CristinProject cristinProject) {
        cristinProject.setStatus(ProjectStatus.ACTIVE.getCristinStatus());
    }
}
