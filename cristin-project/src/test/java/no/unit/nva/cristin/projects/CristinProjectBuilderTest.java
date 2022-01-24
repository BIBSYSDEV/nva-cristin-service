package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.utils.UriUtils;
import nva.commons.core.language.LanguageMapper;
import org.junit.jupiter.api.Test;

import static no.unit.nva.cristin.projects.model.nva.NvaProject.PROJECT_TYPE;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CristinProjectBuilderTest {

    private static final String NORWEGIAN = "no";

    @Test
    void shouldRemainValidProjectWhenDoubleConverted() {
        NvaProject expected = createMinimalValidProject();
        NvaProject actual = expected.toCristinProject().toNvaProject();

        assertEquals(expected, actual);
    }

    private NvaProject createMinimalValidProject() {
        NvaProject nvaProject = new NvaProject();
        nvaProject.setType(PROJECT_TYPE);
        nvaProject.setId(UriUtils.createNvaProjectId(randomInteger().toString()));
        nvaProject.setTitle(randomString());
        nvaProject.setLanguage(LanguageMapper.toUri(NORWEGIAN));
//        nvaProject.setAlternativeTitles(List.of(Map.of(NORWEGIAN, randomString())));


        return nvaProject;
    }


}
