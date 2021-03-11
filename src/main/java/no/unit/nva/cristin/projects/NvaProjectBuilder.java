package no.unit.nva.cristin.projects;

import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.cristin.Institution;
import no.unit.nva.cristin.projects.model.cristin.Person;
import no.unit.nva.cristin.projects.model.cristin.Role;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaOrganization;
import no.unit.nva.cristin.projects.model.nva.NvaPerson;
import no.unit.nva.cristin.projects.model.nva.NvaProject;

public class NvaProjectBuilder {

    private static final String PROJECT_CONTEXT_URL = "https://example.org/search-api-context.json";
    private static final String NVA_PROJECT_BASE_URL = "https://sandbox.nva.unit.no/project";
    private static final String CRISTIN_INSTITUTION_BASE_URI = "https://api.cristin.no/v2/institutions";
    private static final String CRISTIN_PERSON_BASE_URL = "https://api.cristin.no/v2/persons";
    private static final String TEMPORARY_LANGUAGE_URL = "https://lexvo.org/id/iso639-3/nno";

    private static final String PROJECT_TYPE = "Project";
    private static final String CRISTIN_IDENTIFIER_TYPE = "CristinIdentifier";
    private static final String ORGANIZATION_TYPE = "Organization";
    private static final String PERSON_TYPE = "Person";

    private static final String TYPE = "type";
    private static final String VALUE = "value";

    private static final Map<String, String> cristinRolesToNva = Map.of("PRO_MANAGER", "ProjectManager",
        "PRO_PARTICIPANT", "ProjectParticipant");

    public static NvaProject getNvaProjectFromCristinProject(CristinProject cristinProject) {
        NvaProject nvaProject = new NvaProject();

        nvaProject.setContext(PROJECT_CONTEXT_URL);
        nvaProject.setId(buildUri(NVA_PROJECT_BASE_URL, cristinProject.cristinProjectId));
        nvaProject.setType(PROJECT_TYPE);
        nvaProject.setIdentifier(
            Collections.singletonList(Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE, VALUE, cristinProject.cristinProjectId)));
        nvaProject.setTitle(cristinProject.title.get(cristinProject.mainLanguage));
        nvaProject.setLanguage(buildUri(TEMPORARY_LANGUAGE_URL));
        nvaProject.setAlternativeTitles(getAlternativeTitlesFromCristinModel(cristinProject));
        nvaProject.setStartDate(cristinProject.startDate);
        nvaProject.setEndDate(cristinProject.endDate);
        nvaProject.setCoordinatingInstitution(getNvaOrganizationFromCristinModel(
            cristinProject.coordinatingInstitution.institution));
        nvaProject.setContributors(getAllNvaContributorsFromCristinModel(cristinProject.participants));

        return nvaProject;
    }

    private static List<NvaContributor> getAllNvaContributorsFromCristinModel(List<Person> participants) {
        List<NvaContributor> nvaContributors = new ArrayList<>();
        participants.forEach(person -> person.roles.forEach(role -> {
            nvaContributors.add(getNvaContributorFromCristinModel(person, role));
        }));
        return nvaContributors;
    }

    private static NvaContributor getNvaContributorFromCristinModel(Person person, Role role) {
        NvaContributor nvaContributor = new NvaContributor();
        nvaContributor.setType(cristinRolesToNva.get(role.roleCode));
        nvaContributor.setIdentity(getNvaPersonFromCristinModel(person));
        nvaContributor.setAffiliation(getNvaOrganizationFromCristinModel(role.institution));
        return nvaContributor;
    }

    private static NvaPerson getNvaPersonFromCristinModel(Person person) {
        NvaPerson identity = new NvaPerson();
        identity.setId(buildUri(CRISTIN_PERSON_BASE_URL, person.cristinPersonId));
        identity.setType(PERSON_TYPE);
        identity.setFirstName(person.firstName);
        identity.setLastName(person.surname);
        return identity;
    }

    private static NvaOrganization getNvaOrganizationFromCristinModel(Institution institution) {
        NvaOrganization nvaOrganization = new NvaOrganization();
        nvaOrganization.setId(buildUri(CRISTIN_INSTITUTION_BASE_URI, institution.cristinInstitutionId));
        nvaOrganization.setType(ORGANIZATION_TYPE);
        nvaOrganization.setName(institution.institutionName);
        return nvaOrganization;
    }

    private static List<Map<String, String>> getAlternativeTitlesFromCristinModel(CristinProject cristinProject) {
        return Collections.singletonList(
            cristinProject.title.entrySet().stream()
                .filter(excludeMainLanguageFromMapFilter(cristinProject.mainLanguage))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private static Predicate<Entry<String, String>> excludeMainLanguageFromMapFilter(String mainLanguage) {
        return entry -> !entry.getKey().equals(mainLanguage);
    }

    private static URI buildUri(String... parts) {
        return attempt(() -> new URI(String.join("/", parts))).orElse(failure -> null);
    }
}
