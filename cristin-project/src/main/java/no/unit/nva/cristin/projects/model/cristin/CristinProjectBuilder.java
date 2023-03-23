package no.unit.nva.cristin.projects.model.cristin;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.projects.model.nva.Approval;
import no.unit.nva.cristin.projects.model.nva.ContactInfo;
import no.unit.nva.cristin.projects.model.nva.ExternalSource;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.HealthProjectData;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.TypedLabel;
import no.unit.nva.model.Organization;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import no.unit.nva.utils.UriUtils;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.PROJECTS_PATH;
import static no.unit.nva.cristin.model.CristinOrganizationBuilder.fromOrganizationContainingInstitution;
import static no.unit.nva.cristin.model.CristinOrganizationBuilder.fromOrganizationContainingUnitIfPresent;
import static no.unit.nva.language.LanguageMapper.getLanguageByUri;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;

public class CristinProjectBuilder {

    public static final String DEFAULT_TITLE_LANGUAGE_KEY = "nb";
    public static final String PATH_DELIMITER = "/";

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
        cristinProject.setProjectFundingSources(extractFundings(nvaProject.getNewFunding()));
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

        return cristinProject;
    }

    private CristinPerson extractCreator(NvaContributor creator) {
        return Optional.ofNullable(creator).map(NvaContributor::toCristinPersonWithRoles).orElse(null);
    }

    public static void removeFieldsNotSupportedByPost(CristinProject cristinProject) {
        cristinProject.setProjectCategories(removeLabels(cristinProject.getProjectCategories()));
        cristinProject.setKeywords(removeLabels(cristinProject.getKeywords()));
    }

    private static List<CristinTypedLabel> removeLabels(List<CristinTypedLabel> typedLabels) {
        return typedLabels.stream()
                   .map(category -> new CristinTypedLabel(category.getCode(), null))
                   .collect(Collectors.toList());
    }

    private List<CristinApproval> extractApprovals(List<Approval> approvals) {
        return approvals.stream().map(this::toCristinApproval).collect(Collectors.toList());
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
        return typedLabels.stream().map(this::toCristinTypedLabel).collect(Collectors.toList());
    }

    private CristinTypedLabel toCristinTypedLabel(TypedLabel typedLabel) {
        return new CristinTypedLabel(typedLabel.getType(), typedLabel.getLabel());
    }

    private List<CristinExternalSource> extractExternalSources(List<ExternalSource> nvaExternalSources) {
        return nonNull(nvaExternalSources)
                   ? nvaExternalSources.stream()
                         .map(CristinProjectBuilder::toCristinExternalSource)
                         .collect(Collectors.toList())
                   : null;
    }

    private String extractHealthProjectType(HealthProjectData healthProjectData) {
        return Optional.ofNullable(healthProjectData)
                   .map(HealthProjectData::getType)
                   .map(CristinHealthProjectTypeBuilder::reverseLookup)
                   .orElse(null);
    }

    private Map<String, String> extractHealthProjectTypeName(HealthProjectData healthProjectData) {
        return Optional.ofNullable(healthProjectData).map(HealthProjectData::getLabel).orElse(null);
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

    private String extractMainLanguage(URI language) {
        return nonNull(language) ? getLanguageByUri(language).getIso6391Code() : null;
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
        cristinFundingSource.setFundingSourceCode(extractFundingSourceCode(funding.getSource()));
        cristinFundingSource.setFundingSourceName(funding.getLabels());
        cristinFundingSource.setProjectCode(funding.getIdentifier());
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

    private static CristinExternalSource toCristinExternalSource(ExternalSource externalSource) {
        return new CristinExternalSource(externalSource.getName(),
                                         externalSource.getIdentifier());
    }

    /**
     * Extracts funding source code from URI giving a valid Cristin source code.
     */
    public static String extractFundingSourceCode(URI source) {
        if (isNull(source)) {
            return null;
        }
        var sourceAsText = source.toString();
        var lastElementIndexStart = sourceAsText.lastIndexOf(PATH_DELIMITER) + 1;
        var rawSourceCode = sourceAsText.substring(lastElementIndexStart);
        return URLDecoder.decode(rawSourceCode, StandardCharsets.UTF_8);
    }
}
