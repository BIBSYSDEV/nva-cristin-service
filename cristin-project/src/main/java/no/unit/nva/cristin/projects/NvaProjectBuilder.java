package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.UriUtils.getNvaProjectUriWithId;
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
import nva.commons.core.language.LanguageMapper;

public class NvaProjectBuilder {

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
        nvaProject.setId(getNvaProjectUriWithId(cristinProject.getCristinProjectId()));
        nvaProject.setType(PROJECT_TYPE);
        nvaProject.setIdentifiers(createCristinIdentifier());
        nvaProject.setTitle(extractMainTitle());
        nvaProject.setAlternativeTitles(extractAlternativeTitles());
        nvaProject.setLanguage(LanguageMapper.toUri(cristinProject.getMainLanguage()));
        nvaProject.setStartDate(cristinProject.getStartDate());
        nvaProject.setEndDate(cristinProject.getEndDate());
        nvaProject.setGrants(Collections.emptyList());
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
            Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE, VALUE, cristinProject.getCristinProjectId()));
    }

    private static Stream<NvaContributor> generateRoleBasedContribution(CristinPerson cristinPerson) {
        return cristinPerson.getRoles().stream()
            .map(role -> createNvaContributorFromCristinPersonByRole(cristinPerson, role));
    }

    private static NvaContributor createNvaContributorFromCristinPersonByRole(CristinPerson cristinPerson,
                                                                              CristinRole role) {
        NvaContributor nvaContributor = new NvaContributor();
        nvaContributor.setType(cristinRolesToNva.get(role.getRoleCode()));
        nvaContributor.setIdentity(NvaPerson.fromCristinPerson(cristinPerson));
        nvaContributor.setAffiliation(NvaOrganization.fromCristinInstitution(role.getInstitution()));
        return nvaContributor;
    }

    private NvaOrganization extractCoordinatingInstitution() {
        return Optional.ofNullable(cristinProject.getCoordinatingInstitution())
            .map(coordinatingInstitution -> NvaOrganization
                .fromCristinInstitution(coordinatingInstitution.getInstitution()))
            .orElse(null);
    }

    private String extractMainTitle() {
        return Optional.ofNullable(cristinProject.getTitle())
            .map(titles -> titles.get(cristinProject.getMainLanguage()))
            .orElse(null);
    }

    private List<Map<String, String>> extractAlternativeTitles() {
        return Optional.ofNullable(cristinProject.getTitle())
            .filter(titles -> titles.keySet().remove(cristinProject.getMainLanguage()))
            .filter(remainingTitles -> !remainingTitles.isEmpty())
            .map(Collections::singletonList)
            .orElse(Collections.emptyList());
    }

    private List<NvaContributor> extractContributors() {
        return Optional.ofNullable(cristinProject.getParticipants())
            .map(NvaProjectBuilder::transformCristinPersonsToNvaContributors)
            .orElse(Collections.emptyList());
    }

    public NvaProjectBuilder withContext(String context) {
        this.nvaProject.setContext(context);
        return this;
    }

}
