package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.FundingSource;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.UriUtils;
import nva.commons.core.language.LanguageMapper;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.INSTITUTION_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.CRISTIN_IDENTIFIER_TYPE;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.PROJECT_TYPE;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.TYPE;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.VALUE;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomInstant;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;

public class RandomProjectDataGenerator {


    private static final String NORWEGIAN = "no";
    private static final String[] LANGUAGES = {"en", "nb", "nn"};
    private static final String[] CONTRIBUTOR_TYPES = {"ProjectManager", "ProjectParticipant"};

    /**
     * Create a NvaProject containing random data.
     *
     * @return valid NvaProject with random data
     */
    public static NvaProject randomNvaProject() {
        final String identifier = randomInteger().toString();
        final NvaProject nvaProject = new NvaProject.Builder()
                .withType(PROJECT_TYPE)
                .withId(UriUtils.createNvaProjectId(identifier))
                .withIdentifiers(Collections.singletonList(Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE, VALUE, identifier)))
                .withTitle(randomString())
                .withLanguage(LanguageMapper.toUri(NORWEGIAN))
                .withStatus(randomElement(ProjectStatus.values()))
                .withAlternativeTitles(randomListOfTitles())
                .withStartDate(randomInstant())
                .withEndDate(randomInstant())
                .withAcademicSummary(randomSummary())
                .withPopularScientificSummary(randomSummary())
                .withFunding(randomFundings())
                .withContributors(randomContributors())
                .withCoordinatingInstitution(randomOrganization())
                .build();
//        assertThat(nvaProject, doesNotHaveEmptyValues());
        return nvaProject;
    }


    private static URI semiRandomPersonId(String identifier) {
        return new UriWrapper(CRISTIN_API_URL).addChild(PERSON_PATH).addChild(identifier).getUri();
    }

    private static URI semiRandomOrganizationId(String identifier) {
        return new UriWrapper(CRISTIN_API_URL).addChild(INSTITUTION_PATH).addChild(identifier).getUri();
    }

    private static List<NvaContributor> randomContributors() {
        return IntStream.rangeClosed(0, randomInteger(5))
                .mapToObj(i -> randomContributor()).collect(Collectors.toList());
    }

    private static NvaContributor randomContributor() {
        NvaContributor contributor = new NvaContributor();
        contributor.setAffiliation(randomOrganization());
        contributor.setIdentity(randomPerson());
        contributor.setType(randomContributorType());
        return contributor;
    }

    private static String randomContributorType() {
        return CONTRIBUTOR_TYPES[randomInteger(CONTRIBUTOR_TYPES.length)];
    }

    private static Person randomPerson() {
        return new Person(semiRandomPersonId(randomString()), randomString(), randomString());
    }

    private static List<Funding> randomFundings() {
        return IntStream.range(0, randomInteger(7)).mapToObj(i -> randomFunding()).collect(Collectors.toList());
    }

    private static Funding randomFunding() {
        String fundingSourceCode = randomString();
        final FundingSource fundingSource = new FundingSource(randomNamesMap(), fundingSourceCode);
        final String fundingCode = randomString();
        Funding funding = new Funding(fundingSource, fundingCode);
        return funding;
    }

    private static Map<String, String> randomNamesMap() {
        return Map.of(randomLanguageCode(), randomString());
    }

    private static Map<String, String> randomSummary() {
        return Collections.unmodifiableMap(Arrays.stream(LANGUAGES)
                .collect(Collectors.toMap(languageCode -> languageCode, languageCode -> randomString())));
    }

    private static Organization randomOrganization() {
        return new Organization.Builder()
                .withId(semiRandomOrganizationId(randomString()))
                .withName(randomNamesMap())
                .build();
    }

    private static List<Map<String, String>> randomListOfTitles() {
        return List.of(Map.of(randomLanguageCode(), randomString()));
    }

    private static String randomLanguageCode() {
        return randomElement(LANGUAGES);
    }

}
