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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.INSTITUTION_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.projects.CristinProjectBuilder.mapMainLanguageToCristin;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.CRISTIN_IDENTIFIER_TYPE;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.PROJECT_TYPE;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.TYPE;
import static no.unit.nva.cristin.projects.NvaProjectBuilder.VALUE;
import static no.unit.nva.hamcrest.DoesNotHaveEmptyValues.doesNotHaveEmptyValuesIgnoringFields;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomInstant;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;

public class RandomProjectDataGenerator {


    public static final String[] LANGUAGES = {"en", "nb", "nn"};
    public static final Set<String> IGNORE_LIST = Set.of(
            ".context",                             // Context is not used in searchResponse
            ".coordinatingInstitution.partOf",      // Ignoring Organization parts as we only nedd Id here
            ".coordinatingInstitution.hasPart",
            ".coordinatingInstitution.context",
            ".coordinatingInstitution.acronym",
            "contributors.affiliation.partOf",
            "contributors.affiliation.hasPart",
            ".contributors.affiliation.context",
            ".contributors.affiliation.acronym"
    );
    private static final String NORWEGIAN = "nb";
    private static final String[] CONTRIBUTOR_TYPES = {"ProjectManager", "ProjectParticipant"};

    /**
     * Create a NvaProject containing random data.
     *
     * @return valid NvaProject with random data
     */
    public static NvaProject randomNvaProject() {
        final String identifier = randomInteger().toString();
        final URI language = LanguageMapper.toUri(NORWEGIAN);
        final NvaProject nvaProject = new NvaProject.Builder()
                // .withContext(PROJECT_CONTEXT)
                .withType(PROJECT_TYPE)
                .withId(UriUtils.createNvaProjectId(identifier))
                .withIdentifiers(Collections.singletonList(Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE, VALUE, identifier)))
                .withTitle(randomString())
                .withLanguage(language)
                .withStatus(randomElement(ProjectStatus.values()))
                .withAlternativeTitles(randomListOfTitles(language))
                .withStartDate(randomInstant())
                .withEndDate(randomInstant())
                .withAcademicSummary(randomSummary())
                .withPopularScientificSummary(randomSummary())
                .withFunding(randomFundings())
                .withContributors(randomContributors())
                .withCoordinatingInstitution(randomOrganization())
                .build();
        assertThat(nvaProject, doesNotHaveEmptyValuesIgnoringFields(IGNORE_LIST));
        return nvaProject;
    }

    /**
     * Create a valid NvaProject containing so less as possible random data.
     *
     * @return valid NvaProject with random data
     */
    public static NvaProject randomMinimalNvaProject() {
        final String identifier = randomString();
        return new NvaProject.Builder()
                .withId(UriUtils.createNvaProjectId(identifier))
                .withIdentifiers(Collections.singletonList(Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE, VALUE, identifier)))
                .withType(PROJECT_TYPE)
                .withTitle(randomString())
                .withLanguage(LanguageMapper.toUri(randomElement(LANGUAGES)))
                .withStatus(randomElement(ProjectStatus.values()))
                .build();
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
        return randomElement(CONTRIBUTOR_TYPES);
    }

    private static Person randomPerson() {
        return new Person(semiRandomPersonId(randomString()), randomString(), randomString());
    }

    private static List<Funding> randomFundings() {
        return IntStream.rangeClosed(0, randomInteger(7))
                .mapToObj(i -> randomFunding()).collect(Collectors.toList());
    }

    private static Funding randomFunding() {
        return new Funding(new FundingSource(randomNamesMap(), randomString()), randomString());
    }

    private static Map<String, String> randomNamesMap() {
        return Map.of(randomLanguage(), randomString());
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

    private static List<Map<String, String>> randomListOfTitles(URI usedLanguage) {
        return List.of(Map.of(randomLanguageCodeExcept(usedLanguage), randomString()));
    }

    private static String randomLanguage() {
        return randomElement(LANGUAGES);
    }

    private static String randomLanguageCodeExcept(URI usedLanguage) {
        String usedLanguageCode = mapMainLanguageToCristin(usedLanguage);
        String lang = randomElement(LANGUAGES);
        while (lang.equals(usedLanguageCode)) {
            lang = randomElement(LANGUAGES);
        }
        return lang;
    }


}
