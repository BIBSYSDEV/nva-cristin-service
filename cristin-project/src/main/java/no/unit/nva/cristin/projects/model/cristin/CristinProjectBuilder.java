package no.unit.nva.cristin.projects.model.cristin;

import java.net.URI;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.model.Organization;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.CristinOrganizationBuilder.fromOrganizationContainingInstitution;
import static no.unit.nva.cristin.model.CristinOrganizationBuilder.fromOrganizationContainingUnitIfPresent;
import static no.unit.nva.language.LanguageMapper.getLanguageByUri;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;

public class CristinProjectBuilder {

    public static final String DEFAULT_TITLE_LANGUAGE_KEY = "nb";

    private final transient CristinProject cristinProject;
    private final transient NvaProject nvaProject;

    public CristinProjectBuilder(NvaProject nvaProject) {
        this.nvaProject = nvaProject;
        this.cristinProject = new CristinProject();
    }

    private static List<CristinPerson> extractContributors(List<NvaContributor> contributors) {
        return contributors.stream().map(NvaContributor::toCristinPersonWithRoles).collect(Collectors.toList());
    }

    /**
     * Build an CristinProject representation from given NvaProject.
     *
     * @return valid CristinProject containing data from source NvaProject
     */
    public CristinProject build() {

        cristinProject.setCristinProjectId(extractLastPathElement(nvaProject.getId()));
        cristinProject.setMainLanguage(extractMainLanguage(nvaProject.getLanguage()));
        cristinProject.setTitle(extractTitles(nvaProject));
        cristinProject.setStatus(extractStatus(nvaProject));
        cristinProject.setStartDate(nvaProject.getStartDate());
        cristinProject.setEndDate(nvaProject.getEndDate());
        cristinProject.setAcademicSummary(extractSummary(nvaProject.getAcademicSummary()));
        cristinProject.setPopularScientificSummary(extractSummary(nvaProject.getPopularScientificSummary()));
        cristinProject.setProjectFundingSources(extractFundings(nvaProject.getFunding()));
        cristinProject.setParticipants(extractContributors(nvaProject.getContributors()));
        cristinProject.setCoordinatingInstitution(extractCristinOrganization(nvaProject.getCoordinatingInstitution()));
        cristinProject.setMethod(extractSummary(nvaProject.getMethod()));
        cristinProject.setEquipment(extractSummary(nvaProject.getEquipment()));
        cristinProject.setInstitutionsResponsibleForResearch(
            extractInstitutionsResponsibleForResearch(nvaProject.getInstitutionsResponsibleForResearch()));

        return cristinProject;
    }

    private List<CristinOrganization> extractInstitutionsResponsibleForResearch(
        List<Organization> institutionsResponsibleForResearch) {

        return institutionsResponsibleForResearch.stream()
                   .map(this::extractCristinOrganization)
                   .collect(Collectors.toList());
    }

    private String extractMainLanguage(URI language) {
        return nonNull(language) ? getLanguageByUri(nvaProject.getLanguage()).getIso6391Code() : null;
    }

    private CristinOrganization extractCristinOrganization(Organization organization) {
        return fromOrganizationContainingUnitIfPresent(organization).orElse(fallBackToInstitutionLevel(organization));
    }

    private CristinOrganization fallBackToInstitutionLevel(Organization organization) {
        return fromOrganizationContainingInstitution(organization);
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
        } else {
            titles.put(DEFAULT_TITLE_LANGUAGE_KEY, nvaProject.getTitle());
        }
        nvaProject.getAlternativeTitles().forEach(titles::putAll);
        return titles;
    }

    private String extractStatus(NvaProject project) {
        return nonNull(project.getStatus())
                ? project.getStatus().getCristinStatus()
                : null;
    }
}
