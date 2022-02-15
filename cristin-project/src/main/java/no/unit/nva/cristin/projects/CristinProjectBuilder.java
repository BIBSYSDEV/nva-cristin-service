package no.unit.nva.cristin.projects;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.projects.CristinOrganizationBuilder.fromOrganizationContainingInstitution;
import static no.unit.nva.cristin.projects.CristinOrganizationBuilder.fromOrganizationContainingUnitIfPresent;
import static no.unit.nva.language.LanguageMapper.getLanguageByUri;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import no.unit.nva.cristin.projects.model.cristin.CristinFundingSource;
import no.unit.nva.cristin.projects.model.cristin.CristinOrganization;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.model.Organization;

public class CristinProjectBuilder {

    private final transient CristinProject cristinProject;
    private final transient NvaProject nvaProject;

    public CristinProjectBuilder(NvaProject nvaProject) {
        this.nvaProject = nvaProject;
        this.cristinProject = new CristinProject();
    }

    /**
     * Build an CristinProject representation from given NvaProject.
     *
     * @return valid CristinProject containing data from source NvaProject
     */
    public CristinProject build() {

        cristinProject.setCristinProjectId(extractLastPathElement(nvaProject.getId()));
        cristinProject.setMainLanguage(getLanguageByUri(nvaProject.getLanguage()).getIso6391Code());
        cristinProject.setTitle(extractTitles(nvaProject));
        cristinProject.setStatus(nvaProject.getStatus().name());
        cristinProject.setStartDate(nvaProject.getStartDate());
        cristinProject.setEndDate(nvaProject.getEndDate());
        cristinProject.setAcademicSummary(extractSummary(nvaProject.getAcademicSummary()));
        cristinProject.setPopularScientificSummary(extractSummary(nvaProject.getPopularScientificSummary()));
        cristinProject.setProjectFundingSources(extractFundings(nvaProject.getFunding()));
        cristinProject.setParticipants(extractContributors(nvaProject.getContributors()));
        cristinProject.setCoordinatingInstitution(extractCristinOrganization(nvaProject.getCoordinatingInstitution()));

        return cristinProject;
    }

    private CristinOrganization extractCristinOrganization(Organization organization) {
        return fromOrganizationContainingUnitIfPresent(organization).orElse(fallBackToInstitutionLevel(organization));
    }

    private CristinOrganization fallBackToInstitutionLevel(Organization organization) {
        return fromOrganizationContainingInstitution(organization);
    }

    private static List<CristinPerson> extractContributors(List<NvaContributor> contributors) {
        return contributors.stream().map(NvaContributor::toCristinPersonWithRoles).collect(Collectors.toList());
    }

    private List<CristinFundingSource> extractFundings(List<Funding> fundings) {
        return fundings.stream().map(this::getCristinFundingSource).collect(Collectors.toList());
    }

    private CristinFundingSource getCristinFundingSource(Funding funding) {
        CristinFundingSource cristinFundingSource = new CristinFundingSource();
        cristinFundingSource.setFundingSourceCode(funding.getSource().getCode());
        cristinFundingSource.setFundingSourceName(funding.getSource().getNames());
        cristinFundingSource.setProjectCode(funding.getCode());
        return cristinFundingSource;
    }

    private Map<String, String> extractSummary(Map<String, String> summary) {
        return summary.isEmpty() ? null : Collections.unmodifiableMap(summary);
    }

    private Map<String, String> extractTitles(NvaProject nvaProject) {
        Map<String, String> titles = new ConcurrentHashMap<>();
        if (nonNull(nvaProject.getLanguage())) {
            titles.put(getLanguageByUri(nvaProject.getLanguage()).getIso6391Code(), nvaProject.getTitle());
        }
        nvaProject.getAlternativeTitles().forEach(titles::putAll);
        return titles;
    }
}
