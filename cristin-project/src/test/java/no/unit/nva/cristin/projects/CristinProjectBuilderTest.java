package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.utils.UriUtils;
import nva.commons.core.language.LanguageMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static no.unit.nva.cristin.projects.NvaProjectBuilder.CRISTIN_IDENTIFIER_TYPE;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.PROJECT_TYPE;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.TYPE;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.VALUE;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CristinProjectBuilderTest {

    private static final String NORWEGIAN = "no";

    @Test
    void shouldRemainValidProjectWhenDoubleConverted() {
        NvaProject expected = createMinimalValidProject();
        final CristinProject cristinProject = expected.toCristinProject();
        assertNotNull(cristinProject);
        NvaProject actual = cristinProject.toNvaProject();

        assertEquals(expected, actual);
    }

    private NvaProject createMinimalValidProject() {
        NvaProject nvaProject = new NvaProject();
        nvaProject.setType(PROJECT_TYPE);
        final String identifier = randomInteger().toString();
        nvaProject.setId(UriUtils.createNvaProjectId(identifier));
        nvaProject.setIdentifiers(Collections.singletonList(Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE, VALUE, identifier)));
        nvaProject.setTitle(randomString());
        nvaProject.setLanguage(LanguageMapper.toUri(NORWEGIAN));
        nvaProject.setStatus(ProjectStatus.NOTSTARTED);

        return nvaProject;
    }


}
