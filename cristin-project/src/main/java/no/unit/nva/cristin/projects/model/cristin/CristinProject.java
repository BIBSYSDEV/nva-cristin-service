package no.unit.nva.cristin.projects.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.model.CristinApproval;
import no.unit.nva.cristin.model.CristinDateInfo;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.projects.model.nva.NvaProjectBuilder;
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.utils.CustomInstantSerializer;
import nva.commons.core.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;

@SuppressWarnings({"PMD.TooManyFields", "unused", "PMD.ExcessivePublicCount", "PMD.GodClass"})
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinProject implements JsonSerializable {

    public static final String METHOD = "method";
    public static final String EQUIPMENT = "equipment";
    public static final String CRISTIN_PROJECT_CATEGORIES = "project_categories";
    public static final String KEYWORDS = "keywords";
    public static final String CRISTIN_EXTERNAL_SOURCES = "external_sources";
    public static final String CRISTIN_RELATED_PROJECTS = "related_projects";
    public static final String INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH = "institutions_responsible_for_research";
    public static final String HEALTH_PROJECT_TYPE = "health_project_type";
    public static final String HEALTH_PROJECT_TYPE_NAME = "health_project_type_name";
    public static final String CLINICAL_TRIAL_PHASE = "clinical_trial_phase";
    public static final String APPROVALS = "approvals";
    public static final String EXEMPT_FROM_PUBLIC_DISCLOSURE = "exempt_from_public_disclosure";
    public static final String PROJECT_FUNDING_SOURCES = "project_funding_sources";
    public static final String CRISTIN_ACADEMIC_SUMMARY = "academic_summary";
    public static final String CRISTIN_POPULAR_SCIENTIFIC_SUMMARY = "popular_scientific_summary";
    public static final String CRISTIN_MAIN_LANGUAGE = "main_language";

    private String cristinProjectId;
    private Boolean publishable;
    private Boolean published;
    private Map<String, String> title;
    @JsonProperty(CRISTIN_MAIN_LANGUAGE)
    private String mainLanguage;
    @JsonSerialize(using = CustomInstantSerializer.class)
    private Instant startDate;
    @JsonSerialize(using = CustomInstantSerializer.class)
    private Instant endDate;
    private String status;
    private CristinDateInfo created;
    private CristinDateInfo lastModified;
    private CristinOrganization coordinatingInstitution;
    @JsonProperty(PROJECT_FUNDING_SOURCES)
    private List<CristinFundingSource> projectFundingSources;
    private CristinContactInfo contactInfo;
    private CristinFundingAmount totalFundingAmount;
    private List<CristinPerson> participants;
    @JsonProperty(CRISTIN_ACADEMIC_SUMMARY)
    private Map<String, String> academicSummary;
    @JsonProperty(CRISTIN_POPULAR_SCIENTIFIC_SUMMARY)
    private Map<String, String> popularScientificSummary;
    @JsonProperty(METHOD)
    private Map<String, String> method;
    @JsonProperty(EQUIPMENT)
    private Map<String, String> equipment;
    @JsonProperty(CRISTIN_PROJECT_CATEGORIES)
    private List<CristinTypedLabel> projectCategories;
    @JsonProperty(KEYWORDS)
    private List<CristinTypedLabel> keywords;
    @JsonProperty(CRISTIN_EXTERNAL_SOURCES)
    private List<CristinExternalSource> externalSources;
    @JsonProperty(CRISTIN_RELATED_PROJECTS)
    private List<String> relatedProjects;
    @JsonProperty(INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH)
    private List<CristinOrganization> institutionsResponsibleForResearch;
    @JsonProperty(HEALTH_PROJECT_TYPE)
    private String healthProjectType;
    @JsonProperty(HEALTH_PROJECT_TYPE_NAME)
    private Map<String, String> healthProjectTypeName;
    @JsonProperty(CLINICAL_TRIAL_PHASE)
    private String clinicalTrialPhase;
    @JsonProperty(APPROVALS)
    private List<CristinApproval> approvals;
    @JsonProperty(EXEMPT_FROM_PUBLIC_DISCLOSURE)
    private Boolean exemptFromPublicDisclosure;

    public String getCristinProjectId() {
        return cristinProjectId;
    }

    public void setCristinProjectId(String cristinProjectId) {
        this.cristinProjectId = cristinProjectId;
    }

    public Boolean getPublishable() {
        return publishable;
    }

    public void setPublishable(Boolean publishable) {
        this.publishable = publishable;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public Map<String, String> getTitle() {
        return nonEmptyOrDefault(title);
    }

    public void setTitle(Map<String, String> title) {
        this.title = title;
    }

    public String getMainLanguage() {
        return mainLanguage;
    }

    public void setMainLanguage(String mainLanguage) {
        this.mainLanguage = mainLanguage;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public CristinDateInfo getCreated() {
        return created;
    }

    public void setCreated(CristinDateInfo created) {
        this.created = created;
    }

    public CristinDateInfo getLastModified() {
        return lastModified;
    }

    public void setLastModified(CristinDateInfo lastModified) {
        this.lastModified = lastModified;
    }

    public CristinOrganization getCoordinatingInstitution() {
        return coordinatingInstitution;
    }

    public void setCoordinatingInstitution(CristinOrganization coordinatingInstitution) {
        this.coordinatingInstitution = coordinatingInstitution;
    }

    public List<CristinFundingSource> getProjectFundingSources() {
        return nonEmptyOrDefault(projectFundingSources);
    }

    public void setProjectFundingSources(List<CristinFundingSource> projectFundingSources) {
        this.projectFundingSources = projectFundingSources;
    }

    public CristinContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(CristinContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public CristinFundingAmount getTotalFundingAmount() {
        return totalFundingAmount;
    }

    public void setTotalFundingAmount(CristinFundingAmount totalFundingAmount) {
        this.totalFundingAmount = totalFundingAmount;
    }

    public List<CristinPerson> getParticipants() {
        return nonEmptyOrDefault(participants);
    }

    public void setParticipants(List<CristinPerson> participants) {
        this.participants = participants;
    }

    public Map<String, String> getAcademicSummary() {
        return nonEmptyOrDefault(academicSummary);
    }

    public void setAcademicSummary(Map<String, String> academicSummary) {
        this.academicSummary = academicSummary;
    }

    public Map<String, String> getPopularScientificSummary() {
        return nonEmptyOrDefault(popularScientificSummary);
    }

    public void setPopularScientificSummary(Map<String, String> popularScientificSummary) {
        this.popularScientificSummary = popularScientificSummary;
    }

    public Map<String, String> getMethod() {
        return nonEmptyOrDefault(method);
    }

    public void setMethod(Map<String, String> method) {
        this.method = method;
    }

    public Map<String, String> getEquipment() {
        return nonEmptyOrDefault(equipment);
    }

    public void setEquipment(Map<String, String> equipment) {
        this.equipment = equipment;
    }

    public List<CristinTypedLabel> getProjectCategories() {
        return nonEmptyOrDefault(projectCategories);
    }

    public void setProjectCategories(List<CristinTypedLabel> projectCategories) {
        this.projectCategories = projectCategories;
    }

    public List<CristinTypedLabel> getKeywords() {
        return nonEmptyOrDefault(keywords);
    }

    public void setKeywords(List<CristinTypedLabel> keywords) {
        this.keywords = keywords;
    }

    public List<CristinExternalSource> getExternalSources() {
        return nonEmptyOrDefault(externalSources);
    }

    public void setExternalSources(List<CristinExternalSource> externalSources) {
        this.externalSources = externalSources;
    }

    public List<String> getRelatedProjects() {
        return nonEmptyOrDefault(relatedProjects);
    }

    public void setRelatedProjects(List<String> relatedProjects) {
        this.relatedProjects = relatedProjects;
    }

    public List<CristinOrganization> getInstitutionsResponsibleForResearch() {
        return nonEmptyOrDefault(institutionsResponsibleForResearch);
    }

    public void setInstitutionsResponsibleForResearch(List<CristinOrganization> institutionsResponsibleForResearch) {
        this.institutionsResponsibleForResearch = institutionsResponsibleForResearch;
    }

    public String getHealthProjectType() {
        return healthProjectType;
    }

    public void setHealthProjectType(String healthProjectType) {
        this.healthProjectType = healthProjectType;
    }

    public Map<String, String> getHealthProjectTypeName() {
        return nonEmptyOrDefault(healthProjectTypeName);
    }

    public void setHealthProjectTypeName(Map<String, String> healthProjectTypeName) {
        this.healthProjectTypeName = healthProjectTypeName;
    }

    public String getClinicalTrialPhase() {
        return clinicalTrialPhase;
    }

    public void setClinicalTrialPhase(String clinicalTrialPhase) {
        this.clinicalTrialPhase = clinicalTrialPhase;
    }

    public List<CristinApproval> getApprovals() {
        return nonEmptyOrDefault(approvals);
    }

    public void setApprovals(List<CristinApproval> approvals) {
        this.approvals = approvals;
    }

    public Boolean getExemptFromPublicDisclosure() {
        return exemptFromPublicDisclosure;
    }

    public void setExemptFromPublicDisclosure(Boolean exemptFromPublicDisclosure) {
        this.exemptFromPublicDisclosure = exemptFromPublicDisclosure;
    }

    /**
     * Verifies CristinProject has enough data to be considered as valid.
     *
     * @return project has enough data to be considered valid
     */
    public boolean hasValidContent() {
        return StringUtils.isNotBlank(cristinProjectId)
                && !getTitle().isEmpty()
                && ProjectStatus.isValidStatus(status);
    }

    public NvaProject toNvaProject() {
        return new NvaProjectBuilder(this).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinProject)) {
            return false;
        }
        CristinProject that = (CristinProject) o;
        return Objects.equals(getCristinProjectId(), that.getCristinProjectId())
               && Objects.equals(getPublishable(), that.getPublishable())
               && Objects.equals(getPublished(), that.getPublished())
               && Objects.equals(getTitle(), that.getTitle())
               && Objects.equals(getMainLanguage(), that.getMainLanguage())
               && Objects.equals(getStartDate(), that.getStartDate())
               && Objects.equals(getEndDate(), that.getEndDate())
               && Objects.equals(getStatus(), that.getStatus())
               && Objects.equals(getCreated(), that.getCreated())
               && Objects.equals(getLastModified(), that.getLastModified())
               && Objects.equals(getCoordinatingInstitution(), that.getCoordinatingInstitution())
               && Objects.equals(getProjectFundingSources(), that.getProjectFundingSources())
               && Objects.equals(getContactInfo(), that.getContactInfo())
               && Objects.equals(getTotalFundingAmount(), that.getTotalFundingAmount())
               && Objects.equals(getParticipants(), that.getParticipants())
               && Objects.equals(getAcademicSummary(), that.getAcademicSummary())
               && Objects.equals(getPopularScientificSummary(), that.getPopularScientificSummary())
               && Objects.equals(getMethod(), that.getMethod())
               && Objects.equals(getEquipment(), that.getEquipment())
               && Objects.equals(getProjectCategories(), that.getProjectCategories())
               && Objects.equals(getKeywords(), that.getKeywords())
               && Objects.equals(getExternalSources(), that.getExternalSources())
               && Objects.equals(getRelatedProjects(), that.getRelatedProjects())
               && Objects.equals(getInstitutionsResponsibleForResearch(), that.getInstitutionsResponsibleForResearch())
               && Objects.equals(getHealthProjectType(), that.getHealthProjectType())
               && Objects.equals(getHealthProjectTypeName(), that.getHealthProjectTypeName())
               && Objects.equals(getClinicalTrialPhase(), that.getClinicalTrialPhase())
               && Objects.equals(getApprovals(), that.getApprovals())
               && Objects.equals(getExemptFromPublicDisclosure(), that.getExemptFromPublicDisclosure());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCristinProjectId(), getPublishable(), getPublished(), getTitle(), getMainLanguage(),
                            getStartDate(), getEndDate(), getStatus(), getCreated(), getLastModified(),
                            getCoordinatingInstitution(), getProjectFundingSources(), getContactInfo(),
                            getTotalFundingAmount(), getParticipants(), getAcademicSummary(),
                            getPopularScientificSummary(),
                            getMethod(), getEquipment(), getProjectCategories(), getKeywords(), getExternalSources(),
                            getRelatedProjects(), getInstitutionsResponsibleForResearch(), getHealthProjectType(),
                            getHealthProjectTypeName(), getClinicalTrialPhase(), getApprovals(),
                            getExemptFromPublicDisclosure());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

}

