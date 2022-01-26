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

public class RandomProjectDataGenerator {


    private static final String NORWEGIAN = "no";
    private static final String[] LANGUAGES = {"en", "nb", "nn"};
    private static final String[] CONTRIBUTOR_TYPES = {"ProjectManager", "ProjectParticipant"};
    private static final String[] ROLE_TYPES = {"PRO_MANAGER", "PRO_PARTICIPANT"};


    public static NvaProject randomNvaProject() {
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
        nvaProject.setFunding(randomFundings());
        nvaProject.setContributors(randomContributors());
        nvaProject.setCoordinatingInstitution(randomOrganization());

        return nvaProject;
    }


    private static URI semiRandomPersonId(String identifier) {
        return new UriWrapper(CRISTIN_API_URL).addChild(PERSON_PATH).addChild(identifier).getUri();
    }


    private static URI semiRandomOrganizationId(String identifier) {
        return new UriWrapper(CRISTIN_API_URL).addChild(INSTITUTION_PATH).addChild(identifier).getUri();
    }

    private static List<NvaContributor> randomContributors() {
        List<NvaContributor> contributors = new ArrayList<>();
        for (int i = 0; i <= randomInteger(5); i++) {
            NvaContributor contributor = randomContributor();
            contributors.add(contributor);
        }
        return contributors;
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
        List<Funding> fundings = new ArrayList<>();
        for (int i = 0; i < randomInteger(7); i++) {
            Funding funding = randomFunding();
            fundings.add(funding);
        }
        return fundings;
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
        return LANGUAGES[randomInteger(LANGUAGES.length)];
    }


}
