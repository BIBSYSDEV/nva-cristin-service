package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static nva.commons.core.attempt.Try.attempt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Path;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.Test;

public class NvaProjectBuilderTest {

    private static final String API_RESPONSE_ONE_CRISTIN_PROJECT_TO_NVA_PROJECT_JSON =
        "api_response_one_cristin_project_to_nva_project.json";
    private static final String CRISTIN_GET_PROJECT_RESPONSE = "cristinGetProjectResponse.json";

    @Test
    void returnNvaProjectWhenCallingNvaProjectBuilderMethodWithValidCristinProject() throws Exception {
        String expected = IoUtils.stringFromResources(Path.of(API_RESPONSE_ONE_CRISTIN_PROJECT_TO_NVA_PROJECT_JSON));
        String cristinGetProject = IoUtils.stringFromResources(Path.of(CRISTIN_GET_PROJECT_RESPONSE));
        CristinProject cristinProject =
            attempt(() -> OBJECT_MAPPER.readValue(cristinGetProject, CristinProject.class)).get();
        NvaProject nvaProject = new NvaProjectBuilder(cristinProject).build();
        nvaProject.setContext(PROJECT_LOOKUP_CONTEXT_URL);
        String actual = attempt(() -> OBJECT_MAPPER.writeValueAsString(nvaProject)).get();

        assertEquals(OBJECT_MAPPER.readTree(expected), OBJECT_MAPPER.readTree(actual));
    }
}
