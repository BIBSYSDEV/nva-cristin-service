package no.unit.nva.cristin.projects.model.cristin;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinApproval;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.projects.model.nva.Approval;
import no.unit.nva.cristin.projects.model.nva.ContactInfo;
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
import no.unit.nva.model.ExternalSource;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.HealthProjectData;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.model.Organization;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.model.adapter.TypedLabelToCristinFormat;
import no.unit.nva.utils.UriUtils;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.PROJECTS_PATH;
import static no.unit.nva.cristin.model.CristinOrganizationBuilder.fromOrganizationContainingInstitution;
import static no.unit.nva.cristin.model.CristinOrganizationBuilder.fromOrganizationContainingUnitIfPresent;
import static no.unit.nva.cristin.projects.model.cristin.CristinFundingSource.extractFundingSourceCode;
import static no.unit.nva.cristin.projects.util.LanguageUtil.extractLanguageIso6391;
import static no.unit.nva.cristin.projects.util.LanguageUtil.extractTitles;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;

public class CristinProjectBuilder implements Function<NvaProject, CristinProject> {

    private transient CristinProject cristinProject;
    private transient NvaProject nvaProject;

    @Override
    public CristinProject apply(NvaProject nvaProject) {
        this.nvaProject = nvaProject;
        this.cristinProject = new CristinProject();

        return build();
    }

    /**
     * Build an CristinProject representation from given NvaProject.
     *
     * @return valid CristinProject containing data from source NvaProject
     */
    private CristinProject build() {
        cristinProject.setCristinProjectId(extractLastPathElement(nvaProject.getId()));
        cristinProject.setMainLanguage(extractLanguageIso6391(nvaProject.getLanguage()));
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
        cristinProject.setHealthProjectType(extractHealthProjectType(nvaProject.getHealthProjectData()));
        cristinProject.setHealthProjectTypeName(extractHealthProjectTypeName(nvaProject.getHealthProjectData()));
        cristinProject.setClinicalTrialPhase(extractHealthProjectClinicalTrialPhase(nvaProject.getHealthProjectData()));
        cristinProject.setExternalSources(extractExternalSources(nvaProject.getExternalSources()));
        cristinProject.setApprovals(extractApprovals(nvaProject.getApprovals()));
        cristinProject.setKeywords(extractCristinTypedLabels(nvaProject.getKeywords()));
        cristinProject.setProjectCategories(extractCristinTypedLabels(nvaProject.getProjectCategories()));
        cristinProject.setRelatedProjects(extractRelatedProjects(nvaProject.getRelatedProjects()));
        cristinProject.setContactInfo(extractContactInfo(nvaProject.getContactInfo()));
        cristinProject.setExemptFromPublicDisclosure(nvaProject.getExemptFromPublicDisclosure());
        cristinProject.setCreator(extractCreator(nvaProject.getCreator()));
        cristinProject.setExternalUrl(extractExternalUrl(nvaProject.getWebPage()));

        return cristinProject;
    }

    private List<CristinPerson> extractContributors(List<NvaContributor> contributors) {
        return contributors.stream()
                   .map(NvaContributor::toCristinPersonWithRoles)
                   .collect(Collectors.toList());
    }

    private String extractExternalUrl(URI webPage) {
        return Optional.ofNullable(webPage)
                   .map(URI::toString)
                   .orElse(null);
    }

    private CristinPerson extractCreator(NvaContributor creator) {
        if (nonNull(creator)) {
            if (nonNull(creator.getAffiliation())) {
                return creator.toCristinPersonWithRoles();
            } else {
                return creator.getIdentity().toCristinPersonWithoutRoles();
            }
        }
        return null;
    }

    private List<CristinApproval> extractApprovals(List<Approval> approvals) {
        return approvals.stream()
                   .map(this::toCristinApproval)
                   .collect(Collectors.toList());
    }

    /**
     * Converts object of type Approval to object of type CristinApproval.
     */
    private CristinApproval toCristinApproval(Approval approval) {
        return new CristinApproval(approval.getDate(),
                                   CristinApprovalAuthorityBuilder.reverseLookup(approval.getAuthority()),
                                   CristinApprovalStatusBuilder.reverseLookup(approval.getStatus()),
                                   CristinApplicationCodeBuilder.reverseLookup(approval.getApplicationCode()),
                                   approval.getIdentifier(),
                                   approval.getAuthorityName());
    }

    private CristinContactInfo extractContactInfo(ContactInfo contactInfo) {
        if (isNull(contactInfo)) {
            return null;
        }
        return new CristinContactInfo(contactInfo.getContactPerson(),
                                      contactInfo.getOrganization(),
                                      contactInfo.getEmail(),
                                      contactInfo.getPhone());
    }

    private List<String> extractRelatedProjects(List<URI> relatedProjects) {
        return relatedProjects.stream()
                   .map(uri -> UriUtils.nvaIdentifierToCristinIdentifier(uri, PROJECTS_PATH))
                   .map(URI::toString)
                   .collect(Collectors.toList());
    }

    private List<CristinTypedLabel> extractCristinTypedLabels(List<TypedLabel> typedLabels) {
        return typedLabels.stream()
                   .map(new TypedLabelToCristinFormat())
                   .collect(Collectors.toList());
    }

    private List<CristinExternalSource> extractExternalSources(List<ExternalSource> nvaExternalSources) {
        return nonNull(nvaExternalSources)
                   ? nvaExternalSources.stream()
                         .map(this::toCristinExternalSource)
                         .collect(Collectors.toList())
                   : null;
    }

    private CristinExternalSource toCristinExternalSource(ExternalSource externalSource) {
        return new CristinExternalSource(externalSource.getName(), externalSource.getIdentifier());
    }

    private String extractHealthProjectType(HealthProjectData healthProjectData) {
        return Optional.ofNullable(healthProjectData)
                   .map(HealthProjectData::getType)
                   .map(CristinHealthProjectTypeBuilder::reverseLookup)
                   .orElse(null);
    }

    private Map<String, String> extractHealthProjectTypeName(HealthProjectData healthProjectData) {
        return Optional.ofNullable(healthProjectData)
                   .map(HealthProjectData::getLabel)
                   .orElse(null);
    }

    private String extractHealthProjectClinicalTrialPhase(HealthProjectData healthProjectData) {
        return Optional.ofNullable(healthProjectData)
                   .map(HealthProjectData::getClinicalTrialPhase)
                   .map(CristinClinicalTrialPhaseBuilder::reverseLookup)
                   .orElse(null);
    }

    private List<CristinOrganization> extractInstitutionsResponsibleForResearch(
        List<Organization> institutionsResponsibleForResearch) {

        return institutionsResponsibleForResearch.stream()
                   .map(this::extractCristinOrganization)
                   .collect(Collectors.toList());
    }

    private CristinOrganization extractCristinOrganization(Organization organization) {
        return fromOrganizationContainingUnitIfPresent(organization).orElse(fallBackToInstitutionLevel(organization));
    }

    private CristinOrganization fallBackToInstitutionLevel(Organization organization) {
        return fromOrganizationContainingInstitution(organization);
    }

    private List<CristinFundingSource> extractFundings(List<Funding> fundings) {
        return fundings.stream()
                   .map(this::getCristinFundingSource)
                   .collect(Collectors.toList());
    }

    private CristinFundingSource getCristinFundingSource(Funding funding) {
        CristinFundingSource cristinFundingSource = new CristinFundingSource();
        cristinFundingSource.setFundingSourceCode(extractFundingSourceCode(funding.getSource()));
        cristinFundingSource.setFundingSourceName(funding.getLabels());
        cristinFundingSource.setProjectCode(funding.getIdentifier());

        return cristinFundingSource;
    }

    private Map<String, String> extractSummary(Map<String, String> summary) {
        return summary.isEmpty() ? null : Collections.unmodifiableMap(summary);
    }

    private String extractStatus(NvaProject project) {
        return Optional.ofNullable(project.getStatus())
                   .map(ProjectStatus::getCristinStatus)
                   .orElse(null);
    }

}
