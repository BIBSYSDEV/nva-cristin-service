package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.ioutils.IoUtils.stringFromResources;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NvaProjectBuilderTest {

    private static final String API_RESPONSE_ONE_CRISTIN_PROJECT_TO_NVA_PROJECT_WITH_FUNDING_JSON =
        "api_response_one_cristin_project_to_nva_project_with_funding.json";
    private static final String cristinGetProject = stringFromResources(Path.of("cristinGetProjectResponse.json"));
    private static final String CREATED_DATE = "2019-12-31T09:45:17Z";
    private static final String MODIFIED_DATE = "2019-12-31T09:48:20Z";
    private static final String CREATED_BY = "REK";

    @Test
    void shouldReturnNvaProjectWhenCallingNvaProjectBuilderMethodWithValidCristinProject() throws Exception {
        String expected = stringFromResources(
                Path.of(API_RESPONSE_ONE_CRISTIN_PROJECT_TO_NVA_PROJECT_WITH_FUNDING_JSON));
        CristinProject cristinProject =
            attempt(() -> OBJECT_MAPPER.readValue(cristinGetProject, CristinProject.class)).get();
        NvaProject nvaProject = new NvaProjectBuilder(cristinProject).build();
        nvaProject.setContext(PROJECT_LOOKUP_CONTEXT_URL);
        String actual = attempt(() -> OBJECT_MAPPER.writeValueAsString(nvaProject)).get();

        assertEquals(OBJECT_MAPPER.readTree(expected), OBJECT_MAPPER.readTree(actual));
    }

    @Test
    void shouldMapAllSupportedFieldsFoundInCristinJson() {
        var cristinProject =
            attempt(() -> OBJECT_MAPPER.readValue(cristinGetProject, CristinProject.class)).get();
        var nvaProject = new NvaProjectBuilder(cristinProject).build();

        assertThat(nvaProject.getPublished(), equalTo(true));
        assertThat(nvaProject.getPublishable(), equalTo(true));
        assertThat(nvaProject.getCreated().getSourceShortName(), equalTo(CREATED_BY));
        assertThat(nvaProject.getCreated().getDate().toString(), equalTo(CREATED_DATE));
        assertThat(nvaProject.getLastModified().getDate().toString(), equalTo(MODIFIED_DATE));
    }
}
