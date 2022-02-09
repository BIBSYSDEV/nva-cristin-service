package no.unit.nva.cristin.projects;

import java.util.Optional;
import java.util.regex.Pattern;
import no.unit.nva.cristin.projects.model.cristin.CristinFundingSource;
import no.unit.nva.cristin.projects.model.cristin.CristinOrganization;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.cristin.CristinRole;
import no.unit.nva.cristin.projects.model.cristin.CristinUnit;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.model.Organization;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import no.unit.nva.utils.UriUtils;

import static java.util.Objects.nonNull;
import static no.unit.nva.language.LanguageMapper.getLanguageByUri;
import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;
import static no.unit.nva.utils.ContributorRoleMapping.getCristinRole;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;

public class CristinProjectBuilder {

    private static final Pattern UNIT_ID_PATTERN = Pattern.compile(ORGANIZATION_IDENTIFIER_PATTERN);

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
        return Optional.ofNullable(CristinOrganizationBuilder.fromUnitIdentifier(organization))
            .orElse(CristinOrganizationBuilder.buildWithCristinInstitution(organization));
    }

    private List<CristinPerson> extractContributors(List<NvaContributor> contributors) {
        return contributors.stream().map(this::getCristinPerson).collect(Collectors.toList());
    }

    private CristinPerson getCristinPerson(NvaContributor contributor) {
        CristinPerson cristinPerson = contributor.getIdentity().toCristinPerson();
        cristinPerson.setRoles(extractCristinRoles(contributor));
        return cristinPerson;
    }

    private List<CristinRole> extractCristinRoles(NvaContributor contributor) {
        return getCristinRole(contributor.getType()).isPresent()
                ? getCristinRoles(contributor)
                : Collections.emptyList();
    }

    private List<CristinRole> getCristinRoles(NvaContributor contributor) {
        CristinRole cristinRole = new CristinRole();
        if (getCristinRole(contributor.getType()).isPresent()) {
            cristinRole.setRoleCode(getCristinRole(contributor.getType()).get());
        }

        String unitIdentifier = extractUnitIdentifierIfPresent(contributor);

        if (nonNull(unitIdentifier)) {
            cristinRole.setInstitutionUnit(toCristinUnit(unitIdentifier));
        } else {
            cristinRole.setInstitution(contributor.getAffiliation().toCristinInstitution());
        }

        return List.of(cristinRole);
    }

    private CristinUnit toCristinUnit(String unitIdentifier) {
        CristinUnit cristinUnit = new CristinUnit();
        cristinUnit.setCristinUnitId(unitIdentifier);
        return cristinUnit;
    }

    private String extractUnitIdentifierIfPresent(NvaContributor contributor) {
        return Optional.of(contributor).map(NvaContributor::getAffiliation)
            .map(Organization::getId).map(UriUtils::extractLastPathElement)
            .filter(identifier -> UNIT_ID_PATTERN.matcher(identifier).matches())
            .orElse(null);
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
