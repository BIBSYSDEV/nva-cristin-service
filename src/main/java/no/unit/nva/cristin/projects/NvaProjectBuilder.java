package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.Constants.CRISTIN_API_BASE_URL;
import static no.unit.nva.cristin.projects.Constants.INSTITUTION_PATH;
import static no.unit.nva.cristin.projects.Constants.PERSON_PATH;
import static no.unit.nva.cristin.projects.UriUtils.buildUri;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    // TODO: NP-2366: Add dynamic language URIs.
    private static final String TEMPORARY_LANGUAGE_URL = "https://lexvo.org/id/iso639-3/nno";

    private static final String PROJECT_TYPE = "Project";
    private static final String CRISTIN_IDENTIFIER_TYPE = "CristinIdentifier";
    private static final String ORGANIZATION_TYPE = "Organization";
    private static final String PERSON_TYPE = "Person";

    private static final String TYPE = "type";
    private static final String VALUE = "value";

    private static final Map<String, String> cristinRolesToNva = Map.of("PRO_MANAGER", "ProjectManager",
        "PRO_PARTICIPANT", "ProjectParticipant");

    private final transient CristinProject cristinProject;
    private final transient NvaProject nvaProject;

    public NvaProjectBuilder(CristinProject cristinProject) {
        this.cristinProject = cristinProject;
        nvaProject = new NvaProject();
    }

    /**
     * Build a NVA project datamodel from a Cristin project datamodel.
     *
     * @return a NvaProject converted from a CristinProject
     */
    public NvaProject build() {
        // TODO: NP-2384: Remember to use setContext when serializing only a single NvaProject
        nvaProject.setId(buildUri(Constants.BASE_URL, cristinProject.cristinProjectId));
        nvaProject.setType(PROJECT_TYPE);
        nvaProject.setIdentifier(createCristinIdentifier());
        nvaProject.setTitle(extractMainTitle());
        nvaProject.setAlternativeTitles(extractAlternativeTitles());
        nvaProject.setLanguage(buildUri(TEMPORARY_LANGUAGE_URL));
        nvaProject.setStartDate(cristinProject.startDate);
        nvaProject.setEndDate(cristinProject.endDate);
        nvaProject.setCoordinatingInstitution(extractCoordinatingInstitution());
        nvaProject.setContributors(extractContributors());

        return nvaProject;
    }

    private static NvaPerson mapCristinPersonToNvaPerson(Person person) {
        if (person == null) {
            return null;
        }

        NvaPerson identity = new NvaPerson();
        identity.setId(buildUri(CRISTIN_API_BASE_URL, PERSON_PATH, person.cristinPersonId));
        identity.setType(PERSON_TYPE);
        identity.setFirstName(person.firstName);
        identity.setLastName(person.surname);
        return identity;
    }

    private static NvaOrganization mapCristinInstitutionToNvaOrganization(Institution institution) {
        if (institution == null) {
            return null;
        }

        NvaOrganization nvaOrganization = new NvaOrganization();
        nvaOrganization.setId(buildUri(CRISTIN_API_BASE_URL, INSTITUTION_PATH, institution.cristinInstitutionId));
        nvaOrganization.setType(ORGANIZATION_TYPE);
        nvaOrganization.setName(institution.institutionName);
        return nvaOrganization;
    }

    private NvaOrganization extractCoordinatingInstitution() {
        return Optional.ofNullable(cristinProject.coordinatingInstitution)
            .map(elm -> mapCristinInstitutionToNvaOrganization(elm.institution))
            .orElse(null);
    }

    private List<Map<String, String>> createCristinIdentifier() {
        return Collections.singletonList(
            Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE, VALUE, cristinProject.cristinProjectId));
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

    private String extractMainTitle() {
        return Optional.ofNullable(cristinProject.title)
            .map(elm -> elm.get(cristinProject.mainLanguage))
            .orElse(null);
    }

    private List<Map<String, String>> extractAlternativeTitles() {
        return Optional.ofNullable(cristinProject.title)
            .filter(elm -> elm.keySet().remove(cristinProject.mainLanguage))
            .filter(elm -> !elm.isEmpty())
            .map(Collections::singletonList)
            .orElse(null);
    }

    private List<NvaContributor> extractContributors() {
        return Optional.ofNullable(cristinProject.participants)
            .map(NvaProjectBuilder::transformCristinPersonsToNvaContributors)
            .orElse(null);
    }
}
