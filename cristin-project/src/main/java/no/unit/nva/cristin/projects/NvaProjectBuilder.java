package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.projects.model.cristin.CristinFundingSource;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.cristin.CristinRole;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.FundingSource;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.Person;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.ContributorRoleMapping;
import no.unit.nva.utils.UriUtils;
import nva.commons.core.language.LanguageMapper;
import nva.commons.core.paths.UriWrapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;

public class NvaProjectBuilder {

    public static final String CRISTIN_IDENTIFIER_TYPE = "CristinIdentifier";
    public static final String PROJECT_TYPE = "Project";

    public static final String TYPE = "type";
    public static final String VALUE = "value";

    private final transient CristinProject cristinProject;
    private transient String context;

    public NvaProjectBuilder(CristinProject cristinProject) {
        this.cristinProject = cristinProject;
    }

    private static List<NvaContributor> transformCristinPersonsToNvaContributors(List<CristinPerson> participants) {
        return participants.stream()
                .flatMap(NvaProjectBuilder::generateRoleBasedContribution)
                .collect(Collectors.toList());
    }

    private static Stream<NvaContributor> generateRoleBasedContribution(CristinPerson cristinPerson) {
        return cristinPerson.getRoles().stream()
                .map(role -> createNvaContributorFromCristinPersonByRole(cristinPerson, role));
    }

    private static NvaContributor createNvaContributorFromCristinPersonByRole(CristinPerson cristinPerson,
                                                                              CristinRole role) {
        NvaContributor nvaContributor = new NvaContributor();
        if (ContributorRoleMapping.getNvaRole(role.getRoleCode()).isPresent()) {
            nvaContributor.setType(ContributorRoleMapping.getNvaRole(role.getRoleCode()).get());
        }
        nvaContributor.setIdentity(Person.fromCristinPerson(cristinPerson));
        nvaContributor.setAffiliation(role.getInstitution().toOrganization());
        return nvaContributor;
    }

    /**
     * Build a NVA project datamodel from a Cristin project datamodel.
     *
     * @return a NvaProject converted from a CristinProject
     */
    public NvaProject build() {
        return new NvaProject.Builder()
                .withId(new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(UriUtils.PROJECT)
                        .addChild(cristinProject.getCristinProjectId()).getUri())
                .withContext(getContext())
                .withType(PROJECT_TYPE)
                .withIdentifiers(createCristinIdentifier())
                .withTitle(extractMainTitle())
                .withAlternativeTitles(extractAlternativeTitles())
                .withLanguage(LanguageMapper.toUri(cristinProject.getMainLanguage()))
                .withStartDate(cristinProject.getStartDate())
                .withEndDate(cristinProject.getEndDate())
                .withFunding(extractFunding())
                .withCoordinatingInstitution(extractCoordinatingInstitution())
                .withContributors(extractContributors())
                .withStatus(extractProjectStatus())
                .withAcademicSummary(cristinProject.getAcademicSummary())
                .withPopularScientificSummary(cristinProject.getPopularScientificSummary()).build();
    }

    private String getContext() {
        return context;
    }

    private List<Map<String, String>> createCristinIdentifier() {
        return Collections.singletonList(
                Map.of(TYPE, CRISTIN_IDENTIFIER_TYPE, VALUE, cristinProject.getCristinProjectId()));
    }

    private Organization extractCoordinatingInstitution() {
        return Optional.ofNullable(cristinProject.getCoordinatingInstitution())
                .map(coordinatingInstitution -> coordinatingInstitution.getInstitution().toOrganization())
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

    private List<Funding> extractFunding() {
        return cristinProject.getProjectFundingSources().stream()
                .map(this::createFunding)
                .collect(Collectors.toList());
    }

    private Funding createFunding(CristinFundingSource cristinFunding) {
        FundingSource nvaFundingSource =
                new FundingSource(cristinFunding.getFundingSourceName(), cristinFunding.getFundingSourceCode());
        return new Funding(nvaFundingSource, cristinFunding.getProjectCode());
    }

    public NvaProjectBuilder withContext(String context) {
        this.context = context;
        return this;
    }

    private ProjectStatus extractProjectStatus() {
        return ProjectStatus.lookup(cristinProject.getStatus());
    }
}
