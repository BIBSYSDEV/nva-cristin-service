package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.Constants.BASE_URL;
import static no.unit.nva.cristin.projects.UriUtils.buildUri;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.cristin.CristinRole;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaOrganization;
import no.unit.nva.cristin.projects.model.nva.NvaPerson;
import no.unit.nva.cristin.projects.model.nva.NvaProject;

public class NvaProjectBuilder {

    // TODO: NP-2366: Add dynamic language URIs.
    private static final String TEMPORARY_LANGUAGE_URL = "https://lexvo.org/id/iso639-3/nno";

    private static final String PROJECT_TYPE = "Project";
    private static final String CRISTIN_IDENTIFIER_TYPE = "CristinIdentifier";

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
        nvaProject.setId(buildUri(BASE_URL, cristinProject.cristinProjectId));
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

    private static List<NvaContributor> transformCristinPersonsToNvaContributors(List<CristinPerson> participants) {
        return participants.stream()
            .flatMap(NvaProjectBuilder::generateRoleBasedContribution)
            .collect(Collectors.toList());
    }

    private List<Map<String, String>> createCristinIdentifier() {
        return Collections.singletonList(
            Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE, VALUE, cristinProject.cristinProjectId));
    }

    private static Stream<NvaContributor> generateRoleBasedContribution(CristinPerson cristinPerson) {
        return cristinPerson.roles.stream()
            .map(role -> createNvaContributorFromCristinPersonByRole(cristinPerson, role));
    }

    private static NvaContributor createNvaContributorFromCristinPersonByRole(CristinPerson cristinPerson,
                                                                              CristinRole role) {
        NvaContributor nvaContributor = new NvaContributor();
        nvaContributor.setType(cristinRolesToNva.get(role.roleCode));
        nvaContributor.setIdentity(NvaPerson.fromCristinPerson(cristinPerson));
        nvaContributor.setAffiliation(NvaOrganization.fromCristinInstitution(role.institution));
        return nvaContributor;
    }

    private NvaOrganization extractCoordinatingInstitution() {
        return Optional.ofNullable(cristinProject.coordinatingInstitution)
            .map(coordinatingInstitution -> NvaOrganization.fromCristinInstitution(coordinatingInstitution.institution))
            .orElse(null);
    }

    private String extractMainTitle() {
        return Optional.ofNullable(cristinProject.title)
            .map(titles -> titles.get(cristinProject.mainLanguage))
            .orElse(null);
    }

    private List<Map<String, String>> extractAlternativeTitles() {
        return Optional.ofNullable(cristinProject.title)
            .filter(titles -> titles.keySet().remove(cristinProject.mainLanguage))
            .filter(remainingTitles -> !remainingTitles.isEmpty())
            .map(Collections::singletonList)
            .orElse(null);
    }

    private List<NvaContributor> extractContributors() {
        return Optional.ofNullable(cristinProject.participants)
            .map(NvaProjectBuilder::transformCristinPersonsToNvaContributors)
            .orElse(null);
    }
}
