package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.projects.model.nva.ContactInfo;
import no.unit.nva.cristin.projects.model.nva.DateInfo;
import no.unit.nva.cristin.projects.model.nva.ExternalSource;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.FundingAmount;
import no.unit.nva.cristin.projects.model.nva.FundingSource;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
import no.unit.nva.cristin.projects.model.nva.TypedLabel;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.UriUtils;
import nva.commons.core.language.LanguageMapper;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import nva.commons.core.paths.UriWrapper;

import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.cristin.projects.model.nva.NvaProjectBuilder.CRISTIN_IDENTIFIER_TYPE;
import static no.unit.nva.cristin.projects.model.nva.NvaProjectBuilder.PROJECT_TYPE;
import static no.unit.nva.cristin.projects.model.nva.NvaProjectBuilder.TYPE;
import static no.unit.nva.cristin.projects.model.nva.NvaProjectBuilder.VALUE;
import static no.unit.nva.hamcrest.DoesNotHaveEmptyValues.doesNotHaveEmptyValuesIgnoringFields;
import static no.unit.nva.language.LanguageMapper.getLanguageByUri;
import static no.unit.nva.testutils.RandomDataGenerator.randomElement;
import static no.unit.nva.testutils.RandomDataGenerator.randomInstant;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static org.hamcrest.MatcherAssert.assertThat;

public class RandomProjectDataGenerator {


    public static final String[] LANGUAGES = {"en", "nb", "nn"};
    public static final Set<String> IGNORE_LIST = Set.of(
            ".context",                             // Context is not used in searchResponse
            ".coordinatingInstitution.partOf",      // Ignoring Organization parts as we only need Id here
            ".coordinatingInstitution.hasPart",
            ".coordinatingInstitution.context",
            ".coordinatingInstitution.acronym",
            "contributors.affiliation.partOf",
            "contributors.affiliation.hasPart",
            ".contributors.affiliation.context",
            ".contributors.affiliation.acronym",
            "published",
            "publishable",
            ".institutionsResponsibleForResearch.hasPart",
            ".institutionsResponsibleForResearch.partOf",
            ".institutionsResponsibleForResearch.acronym",
            ".institutionsResponsibleForResearch.context"
    );
    private static final String NORWEGIAN = "nb";
    private static final String[] CONTRIBUTOR_TYPES = {"ProjectManager", "ProjectParticipant"};
    public static final String SOME_UNIT_IDENTIFIER = "185.90.0.0";
    public static final String EMAIL_DOMAIN = "@email.no";

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
                                          .withIdentifiers(Collections.singletonList(
                                              Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE, VALUE, identifier)))
                                          .withTitle(randomString())
                                          .withLanguage(language)
                                          .withStatus(randomStatus())
                                          .withAlternativeTitles(randomListOfTitles(language))
                                          .withStartDate(randomInstant())
                                          .withEndDate(randomInstant())
                                          .withAcademicSummary(randomSummary())
                                          .withPopularScientificSummary(randomSummary())
                                          .withFunding(randomFundings())
                                          .withContributors(randomContributors())
                                          .withCoordinatingInstitution(randomOrganization())
                                          .withCreated(randomDateInfo())
                                          .withLastModified(randomDateInfo())
                                          .withContactInfo(randomContactInfo())
                                          .withFundingAmount(randomFundingAmount())
                                          .withMethod(randomSummary())
                                          .withEquipment(randomSummary())
                                          .withProjectCategories(List.of(randomTypedLabel()))
                                          .withKeywords(List.of(randomTypedLabel()))
                                          .withExternalSources(List.of(randomExternalSource()))
                                          .withRelatedProjects(List.of(randomRelatedProjects()))
                                          .withInstitutionsResponsibleForResearch(List.of(randomOrganization()))
                                          .build();
        assertThat(nvaProject, doesNotHaveEmptyValuesIgnoringFields(IGNORE_LIST));
        return nvaProject;
    }

    private static URI randomRelatedProjects() {
        return UriWrapper.fromUri(randomUri()).addChild(randomInteger(99999).toString()).getUri();
    }

    private static ExternalSource randomExternalSource() {
        return new ExternalSource(randomString(), randomString());
    }

    private static TypedLabel randomTypedLabel() {
        return new TypedLabel(randomString(), randomNamesMap());
    }

    private static ContactInfo randomContactInfo() {
        return new ContactInfo(randomString(), randomString(), randomString() + EMAIL_DOMAIN, randomString());
    }

    private static FundingAmount randomFundingAmount() {
        return new FundingAmount(randomString(), randomInteger().doubleValue());
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
                .withStatus(randomStatus())
                .withCoordinatingInstitution(randomOrganization())
                .withContributors(randomContributors())
                .withStartDate(randomInstant())
                .build();
    }

    public static Map<String, String> randomNamesMap() {
        return Map.of(randomLanguage(), randomString());
    }

    public static ProjectStatus randomStatus() {
        return randomElement(ProjectStatus.values());
    }

    private static URI semiRandomPersonId(String identifier) {
        return getNvaApiId(identifier, PERSON_PATH_NVA);
    }

    private static URI semiRandomOrganizationId(String identifier) {
        return getNvaApiId(identifier, ORGANIZATION_PATH);
    }

    public static List<NvaContributor> randomContributors() {
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

    private static Map<String, String> randomSummary() {
        return Collections.unmodifiableMap(Arrays.stream(LANGUAGES)
                .collect(Collectors.toMap(languageCode -> languageCode, languageCode -> randomString())));
    }

    /**
     * Creates a random organization.
     */
    public static Organization randomOrganization() {
        return new Organization.Builder()
                .withId(semiRandomOrganizationId(randomString()))
                .withName(randomNamesMap())
                .build();
    }

    public static List<Map<String, String>> randomListOfTitles(URI usedLanguage) {
        return List.of(Map.of(randomLanguageCodeExcept(usedLanguage), randomString()));
    }

    public static String randomLanguage() {
        return randomElement(LANGUAGES);
    }

    private static String randomLanguageCodeExcept(URI usedLanguage) {
        String usedLanguageCode = getLanguageByUri(usedLanguage).getIso6391Code();
        String lang = randomElement(LANGUAGES);
        while (lang.equals(usedLanguageCode)) {
            lang = randomElement(LANGUAGES);
        }
        return lang;
    }

    /**
     * Creates a random contributor with unit affiliation.
     */
    public static NvaContributor randomContributorWithUnitAffiliation() {
        NvaContributor contributor = new NvaContributor();
        contributor.setAffiliation(someOrganizationFromUnitIdentifier());
        contributor.setIdentity(randomPerson());
        contributor.setType(randomContributorType());
        return contributor;
    }

    /**
     * Creates a random contributor without unit affiliation.
     */
    public static NvaContributor randomContributorWithoutUnitAffiliation() {
        NvaContributor contributor = new NvaContributor();
        contributor.setIdentity(randomPerson());
        contributor.setType(randomContributorType());
        return contributor;
    }

    /**
     * Creates a dummy organization with unit identifier.
     */
    public static Organization someOrganizationFromUnitIdentifier() {
        return new Organization.Builder().withId(getNvaApiId(SOME_UNIT_IDENTIFIER, ORGANIZATION_PATH)).build();
    }

    private static DateInfo randomDateInfo() {
        return new DateInfo(randomString(), randomInstant());
    }
}
