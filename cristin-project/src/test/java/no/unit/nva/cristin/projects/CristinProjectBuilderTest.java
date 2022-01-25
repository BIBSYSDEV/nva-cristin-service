package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.FundingSource;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.UriUtils;
import nva.commons.core.language.LanguageMapper;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.INSTITUTION_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.CRISTIN_IDENTIFIER_TYPE;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.PROJECT_TYPE;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.TYPE;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.VALUE;
import static no.unit.nva.testutils.RandomDataGenerator.randomInstant;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CristinProjectBuilderTest {

    private static final String NORWEGIAN = "no";
    private static final String[] LANGUAGES = {"en", "nb", "nn"};
    private static final String[] CONTRIBUTOR_TYPES = {"ProjectManager", "ProjectParticipant"};
    private static final String[] ROLE_TYPES = {"PRO_MANAGER", "PRO_PARTICIPANT"};


    @Test
    void shouldRemainValidProjectWhenDoubleConverted() {
        final NvaProject expected = createRandomNvaProject();
        final CristinProject cristinProject = expected.toCristinProject();
        assertNotNull(cristinProject);
        NvaProject actual = cristinProject.toNvaProject();

        assertEquals(expected, actual);
    }

    private NvaProject createRandomNvaProject() {
        NvaProject nvaProject = new NvaProject();
        nvaProject.setType(PROJECT_TYPE);
        final String identifier = randomInteger().toString();
        nvaProject.setId(UriUtils.createNvaProjectId(identifier));
        nvaProject.setIdentifiers(Collections.singletonList(Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE, VALUE, identifier)));
        nvaProject.setTitle(randomString());
        nvaProject.setLanguage(LanguageMapper.toUri(NORWEGIAN));
        nvaProject.setStatus(ProjectStatus.NOTSTARTED);
        nvaProject.setAlternativeTitles(randomListOfTitles());
        nvaProject.setStartDate(randomInstant());
        nvaProject.setEndDate(randomInstant());
        nvaProject.setAcademicSummary(randomSummary());
        nvaProject.setPopularScientificSummary(randomSummary());
        nvaProject.setFunding(randomFunding());
        nvaProject.setContributors(randomContributors());
        nvaProject.setCoordinatingInstitution(randomOrganization());

        return nvaProject;
    }

    private URI semiRandomPersonId(String identifier) {
        return new UriWrapper(CRISTIN_API_URL).addChild(PERSON_PATH).addChild(identifier).getUri();
    }


    private URI semiRandomOrganizationId(String identifier) {
        return new UriWrapper(CRISTIN_API_URL).addChild(INSTITUTION_PATH).addChild(identifier).getUri();
    }

    private List<NvaContributor> randomContributors() {
        List<NvaContributor> contributors = new ArrayList<>();
        for (int i = 0; i <= randomInteger(5); i++) {
            NvaContributor contributor = new NvaContributor();
            contributor.setAffiliation(randomOrganization());
            contributor.setIdentity(randomPerson());
            contributor.setType(randomContributorType());
            contributors.add(contributor);
        }
        return contributors;
    }

    private String randomContributorType() {
        return CONTRIBUTOR_TYPES[randomInteger(CONTRIBUTOR_TYPES.length)];
    }

    private Person randomPerson() {
        return new Person(semiRandomPersonId(randomString()), randomString(), randomString());
    }

    private List<Funding> randomFunding() {
        List<Funding> fundings = new ArrayList<>();
        for (int i = 0; i < randomInteger(7); i++) {
            String fundingSourceCode = randomString();
            final FundingSource fundingSource = new FundingSource(randomNamesMap(), fundingSourceCode);
            final String fundingCode = randomString();
            Funding funding = new Funding(fundingSource, fundingCode);
            fundings.add(funding);
        }
        return fundings;
    }

    private Map<String, String> randomNamesMap() {
        return Map.of(randomLanguageCode(), randomString());
    }

    private Map<String, String> randomSummary() {
        return Collections.unmodifiableMap(Arrays.stream(LANGUAGES)
                .collect(Collectors.toMap(languageCode -> languageCode, languageCode -> randomString())));
    }

    private Organization randomOrganization() {
        return new Organization.Builder()
                .withId(semiRandomOrganizationId(randomString()))
                .withName(randomNamesMap())
                .build();
    }

    private List<Map<String, String>> randomListOfTitles() {
        return List.of(Map.of(randomLanguageCode(), randomString()));
    }

    private String randomLanguageCode() {
        return LANGUAGES[randomInteger(LANGUAGES.length)];
    }


}
