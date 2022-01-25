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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CristinProjectBuilder {

    private static final Map<String, String> NvaTypesToCristinRoles = Map.of(
            "ProjectManager",
            "PRO_MANAGER",
            "ProjectParticipant",
            "PRO_PARTICIPANT");
    private final transient CristinProject cristinProject;
    private final transient NvaProject nvaProject;

    public CristinProjectBuilder(NvaProject nvaProject) {
        this.nvaProject = nvaProject;
        this.cristinProject = new CristinProject();
    }

    public static String extractLastPathElement(URI id) {
        return id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
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
        List<CristinPerson> participants = new ArrayList<>();
        for (NvaContributor contributor : contributors) {
            CristinPerson cristinPerson = getCristinPerson(contributor);
            participants.add(cristinPerson);
        }
        return participants;
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
        cristinRole.setRoleCode(NvaTypesToCristinRoles.get(contributor.getType()));
        cristinRole.setInstitution(toCristinInstitution(contributor.getAffiliation()));
        return List.of(cristinRole);
    }

    private CristinInstitution toCristinInstitution(Organization affiliation) {
        CristinInstitution institution = new CristinInstitution();
        institution.setInstitutionName(affiliation.getName());
        institution.setUrl(affiliation.getId().toString());
        institution.setCristinInstitutionId(extractLastPathElement(affiliation.getId()));
        return institution;
    }

    private List<CristinFundingSource> extractFundings(List<Funding> fundings) {
        List<CristinFundingSource> cristinFundings = new ArrayList();
        for (Funding funding : fundings) {
            CristinFundingSource cristinFundingSource = getCristinFundingSource(funding);
            cristinFundings.add(cristinFundingSource);
        }
        return cristinFundings;
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
