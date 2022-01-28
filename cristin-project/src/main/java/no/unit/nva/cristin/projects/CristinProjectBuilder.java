package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.model.CristinInstitution;
import no.unit.nva.cristin.projects.model.cristin.CristinFundingSource;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.cristin.CristinRole;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.ContributorRoleMapping;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static no.unit.nva.utils.UriUtils.extractLastPathElement;

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
        cristinProject.setMainLanguage(extractLastPathElement(nvaProject.getLanguage()));
        cristinProject.setTitle(extractTitles(nvaProject));
        cristinProject.setStatus(nvaProject.getStatus().name());
        cristinProject.setStartDate(nvaProject.getStartDate());
        cristinProject.setEndDate(nvaProject.getEndDate());
        cristinProject.setAcademicSummary(extractSummary(nvaProject.getAcademicSummary()));
        cristinProject.setPopularScientificSummary(extractSummary(nvaProject.getPopularScientificSummary()));
        cristinProject.setProjectFundingSources(extractFundings(nvaProject.getFunding()));
        cristinProject.setParticipants(extractContributors(nvaProject.getContributors()));
        cristinProject.setCoordinatingInstitution(
                new CristinOrganizationBuilder(nvaProject.getCoordinatingInstitution()).build());

        return cristinProject;
    }

    private List<CristinPerson> extractContributors(List<NvaContributor> contributors) {
        return contributors.stream().map(this::getCristinPerson).collect(Collectors.toList());
    }

    private CristinPerson getCristinPerson(NvaContributor contributor) {
        CristinPerson cristinPerson = new CristinPerson();
        cristinPerson.setCristinPersonId(extractLastPathElement(contributor.getIdentity().getId()));
        cristinPerson.setUrl(contributor.getIdentity().getId().toString());
        cristinPerson.setFirstName(contributor.getIdentity().getFirstName());
        cristinPerson.setSurname(contributor.getIdentity().getLastName());
        cristinPerson.setRoles(extractCristinRoles(contributor));
        return cristinPerson;
    }

    private List<CristinRole> extractCristinRoles(NvaContributor contributor) {
        CristinRole cristinRole = new CristinRole();
        cristinRole.setRoleCode(ContributorRoleMapping.getCristinRole(contributor.getType()).get());
        cristinRole.setInstitution(toCristinInstitution(contributor.getAffiliation()));
        return List.of(cristinRole);
    }

    private CristinInstitution toCristinInstitution(Organization organization) {
        CristinInstitution institution = new CristinInstitution();
        institution.setInstitutionName(organization.getName());
        institution.setUrl(organization.getId().toString());
        institution.setCristinInstitutionId(extractLastPathElement(organization.getId()));
        return institution;
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
        return Collections.unmodifiableMap(summary);
    }

    private Map<String, String> extractTitles(NvaProject nvaProject) {
        Map<String, String> titles = new ConcurrentHashMap<>();
        titles.put(extractLastPathElement(nvaProject.getLanguage()), nvaProject.getTitle());
        nvaProject.getAlternativeTitles().stream().forEach(titles::putAll);
        return titles;
    }
}
