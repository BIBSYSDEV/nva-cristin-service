package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.CommonUtil.buildUri;
import static nva.commons.core.attempt.Try.attempt;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.cristin.Institution;
import no.unit.nva.cristin.projects.model.cristin.Person;
import no.unit.nva.cristin.projects.model.cristin.Role;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaOrganization;
import no.unit.nva.cristin.projects.model.nva.NvaPerson;
import no.unit.nva.cristin.projects.model.nva.NvaProject;

public class NvaProjectBuilder {

    //private static final String PROJECT_CONTEXT_URL = "https://example.org/search-api-context.json"; // TODO:
    // Change this?
    private static final String NVA_PROJECT_BASE_URL = "https://api.dev.nva.unit.no/project"; // TODO: Replace "dev"
    // with env var. Remember to mock URL in unit tests or pass in right URL for each environment
    private static final String CRISTIN_INSTITUTION_BASE_URI = "https://api.cristin.no/v2/institutions"; // TODO: Use
    // env var
    private static final String CRISTIN_PERSON_BASE_URL = "https://api.cristin.no/v2/persons"; // TODO: Use env var
    private static final String TEMPORARY_LANGUAGE_URL = "https://lexvo.org/id/iso639-3/nno"; // TODO: Replace with
    // real values

    private static final String PROJECT_TYPE = "Project";
    private static final String CRISTIN_IDENTIFIER_TYPE = "CristinIdentifier";
    private static final String ORGANIZATION_TYPE = "Organization";
    private static final String PERSON_TYPE = "Person";

    private static final String TYPE = "type";
    private static final String VALUE = "value";

    private static final Map<String, String> cristinRolesToNva = Map.of("PRO_MANAGER", "ProjectManager",
        "PRO_PARTICIPANT", "ProjectParticipant");

    /**
     * Build a NVA project datamodel from a Cristin project datamodel.
     *
     * @param cristinProject The model to convert from
     * @return a NvaProject converted from a CristinProject
     */
    public static NvaProject mapCristinProjectToNvaProject(CristinProject cristinProject) {
        NvaProject nvaProject = new NvaProject();

        // TODO: If one NvaProject, put context on top in NvaProject.
        //  If list of NvaProject, put context on top in metadata
        //nvaProject.setContext(PROJECT_CONTEXT_URL);
        nvaProject.setId(buildUri(NVA_PROJECT_BASE_URL, cristinProject.cristinProjectId));
        nvaProject.setType(PROJECT_TYPE);
        nvaProject.setIdentifier(
            Collections.singletonList(Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE, VALUE, cristinProject.cristinProjectId)));
        nvaProject.setTitle(cristinProject.title.get(cristinProject.mainLanguage));
        nvaProject.setLanguage(buildUri(TEMPORARY_LANGUAGE_URL));
        nvaProject.setAlternativeTitles(extractAlternativeTitles(cristinProject));
        nvaProject.setStartDate(cristinProject.startDate);
        nvaProject.setEndDate(cristinProject.endDate);
        nvaProject.setCoordinatingInstitution(attempt(() ->
            mapCristinInstitutionToNvaOrganization(cristinProject.coordinatingInstitution.institution))
            .orElse(failure -> null));
        nvaProject.setContributors(attempt(() ->
            transformCristinPersonsToNvaContributors(cristinProject.participants))
            .orElse(failure -> null));

        return nvaProject;
    }

    private static List<NvaContributor> transformCristinPersonsToNvaContributors(List<Person> participants) {
        return participants.stream()
            .flatMap(NvaProjectBuilder::generateRoleBasedContribution)
            .collect(Collectors.toList());
    }

    private static Stream<NvaContributor> generateRoleBasedContribution(Person person) {
        return person.roles.stream()
            .map(role -> createNvaContributorFromCristinPersonByRole(person, role));
    }

    private static NvaContributor createNvaContributorFromCristinPersonByRole(Person person, Role role) {
        NvaContributor nvaContributor = new NvaContributor();
        nvaContributor.setType(cristinRolesToNva.get(role.roleCode));
        nvaContributor.setIdentity(mapCristinPersonToNvaPerson(person));
        nvaContributor.setAffiliation(mapCristinInstitutionToNvaOrganization(role.institution));
        return nvaContributor;
    }

    private static NvaPerson mapCristinPersonToNvaPerson(Person person) {
        NvaPerson identity = new NvaPerson();
        identity.setId(buildUri(CRISTIN_PERSON_BASE_URL, person.cristinPersonId));
        identity.setType(PERSON_TYPE);
        identity.setFirstName(person.firstName);
        identity.setLastName(person.surname);
        return identity;
    }

    private static NvaOrganization mapCristinInstitutionToNvaOrganization(Institution institution) {
        NvaOrganization nvaOrganization = new NvaOrganization();
        nvaOrganization.setId(buildUri(CRISTIN_INSTITUTION_BASE_URI, institution.cristinInstitutionId));
        nvaOrganization.setType(ORGANIZATION_TYPE);
        nvaOrganization.setName(institution.institutionName);
        return nvaOrganization;
    }

    private static List<Map<String, String>> extractAlternativeTitles(CristinProject cristinProject) {

        Map<String, String> map = cristinProject.title;
        map.remove(cristinProject.mainLanguage);

        return !map.isEmpty() ? Collections.singletonList(map) : null;
    }
}
