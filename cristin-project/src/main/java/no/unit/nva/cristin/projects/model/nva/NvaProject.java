package no.unit.nva.cristin.projects.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import static no.unit.nva.cristin.model.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.projects.model.cristin.CristinProjectBuilder;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.model.ExternalSource;
import no.unit.nva.model.Organization;
import no.unit.nva.model.DateInfo;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.model.UriId;

@SuppressWarnings({"PMD.ExcessivePublicCount", "PMD.TooManyFields", "PMD.GodClass", "PMD"
        + ".ClassWithOnlyPrivateConstructorsShouldBeFinal"})
@JsonInclude(ALWAYS)
public class NvaProject implements JsonSerializable, UriId {

    public static final String PROJECT_CONTEXT = PROJECT_LOOKUP_CONTEXT_URL;
    public static final String PROJECT_TYPE = "Project";

    @JsonProperty(CONTEXT)
    @JsonInclude(NON_NULL)
    private String context;
    @JsonProperty
    private URI id;
    @JsonProperty
    private String type;
    @JsonProperty
    @JsonPropertyOrder(alphabetic = true)
    private List<Map<String, String>> identifiers;
    @JsonProperty
    private String title;
    @JsonProperty
    private URI language;
    @JsonProperty
    @JsonPropertyOrder(alphabetic = true)
    private List<Map<String, String>> alternativeTitles;
    @JsonProperty
    private Instant startDate;
    @JsonProperty
    private Instant endDate;
    @JsonProperty
    private List<Funding> funding;
    @JsonProperty
    private Organization coordinatingInstitution;
    @JsonProperty
    private List<NvaContributor> contributors;
    @JsonProperty
    private ProjectStatus status;
    @JsonProperty
    private Map<String, String> academicSummary;
    @JsonProperty
    private Map<String, String> popularScientificSummary;
    @JsonProperty
    @JsonInclude(NON_NULL)
    private Boolean published;
    @JsonProperty
    @JsonInclude(NON_NULL)
    private Boolean publishable;
    @JsonProperty
    @JsonInclude(NON_NULL)
    private DateInfo created;
    @JsonProperty
    @JsonInclude(NON_NULL)
    private DateInfo lastModified;
    @JsonProperty
    @JsonInclude(NON_NULL)
    private ContactInfo contactInfo;
    @JsonProperty
    @JsonInclude(NON_NULL)
    private FundingAmount fundingAmount;
    @JsonProperty
    private Map<String, String> method;
    @JsonProperty
    private Map<String, String> equipment;
    @JsonProperty
    private List<TypedLabel> projectCategories;
    @JsonProperty
    private List<TypedLabel> keywords;
    @JsonProperty
    private List<ExternalSource> externalSources;
    @JsonProperty
    private List<URI> relatedProjects;
    @JsonProperty
    private List<Organization> institutionsResponsibleForResearch;
    @JsonProperty
    @JsonInclude(NON_NULL)
    private HealthProjectData healthProjectData;
    @JsonProperty
    private List<Approval> approvals;
    @JsonProperty
    @JsonInclude(NON_NULL)
    private Boolean exemptFromPublicDisclosure;
    @JsonProperty
    @JsonInclude(NON_NULL)
    private NvaContributor creator;
    @JsonProperty
    @JsonInclude(NON_NULL)
    private URI webPage;

    private NvaProject() {
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Map<String, String>> getIdentifiers() {
        return nonEmptyOrDefault(identifiers);
    }

    public void setIdentifiers(List<Map<String, String>> identifiers) {
        this.identifiers = identifiers;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public URI getLanguage() {
        return language;
    }

    public void setLanguage(URI language) {
        this.language = language;
    }

    public List<Map<String, String>> getAlternativeTitles() {
        return nonEmptyOrDefault(alternativeTitles);
    }

    public void setAlternativeTitles(List<Map<String, String>> alternativeTitles) {
        this.alternativeTitles = alternativeTitles;
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

    public List<Funding> getFunding() {
        return nonEmptyOrDefault(funding);
    }

    public void setFunding(List<Funding> funding) {
        this.funding = funding;
    }

    public Organization getCoordinatingInstitution() {
        return coordinatingInstitution;
    }

    public void setCoordinatingInstitution(Organization coordinatingInstitution) {
        this.coordinatingInstitution = coordinatingInstitution;
    }

    public List<NvaContributor> getContributors() {
        return nonEmptyOrDefault(contributors);
    }

    public void setContributors(List<NvaContributor> contributors) {
        this.contributors = contributors;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
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

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public Boolean getPublishable() {
        return publishable;
    }

    public void setPublishable(Boolean publishable) {
        this.publishable = publishable;
    }

    public DateInfo getCreated() {
        return created;
    }

    public void setCreated(DateInfo created) {
        this.created = created;
    }

    public DateInfo getLastModified() {
        return lastModified;
    }

    public void setLastModified(DateInfo lastModified) {
        this.lastModified = lastModified;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public FundingAmount getFundingAmount() {
        return fundingAmount;
    }

    public void setFundingAmount(FundingAmount fundingAmount) {
        this.fundingAmount = fundingAmount;
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

    public List<TypedLabel> getProjectCategories() {
        return nonEmptyOrDefault(projectCategories);
    }

    public void setProjectCategories(List<TypedLabel> projectCategories) {
        this.projectCategories = projectCategories;
    }

    public List<TypedLabel> getKeywords() {
        return nonEmptyOrDefault(keywords);
    }

    public void setKeywords(List<TypedLabel> keywords) {
        this.keywords = keywords;
    }

    public List<ExternalSource> getExternalSources() {
        return nonEmptyOrDefault(externalSources);
    }

    public void setExternalSources(List<ExternalSource> externalSources) {
        this.externalSources = externalSources;
    }

    public List<URI> getRelatedProjects() {
        return nonEmptyOrDefault(relatedProjects);
    }

    public void setRelatedProjects(List<URI> relatedProjects) {
        this.relatedProjects = relatedProjects;
    }

    public List<Organization> getInstitutionsResponsibleForResearch() {
        return nonEmptyOrDefault(institutionsResponsibleForResearch);
    }

    public void setInstitutionsResponsibleForResearch(List<Organization> institutionsResponsibleForResearch) {
        this.institutionsResponsibleForResearch = institutionsResponsibleForResearch;
    }

    public HealthProjectData getHealthProjectData() {
        return healthProjectData;
    }

    public void setHealthProjectData(HealthProjectData healthProjectData) {
        this.healthProjectData = healthProjectData;
    }

    public List<Approval> getApprovals() {
        return nonEmptyOrDefault(approvals);
    }

    public void setApprovals(List<Approval> approvals) {
        this.approvals = approvals;
    }

    public Boolean getExemptFromPublicDisclosure() {
        return exemptFromPublicDisclosure;
    }

    public void setExemptFromPublicDisclosure(Boolean exemptFromPublicDisclosure) {
        this.exemptFromPublicDisclosure = exemptFromPublicDisclosure;
    }

    public NvaContributor getCreator() {
        return creator;
    }

    public void setCreator(NvaContributor creator) {
        this.creator = creator;
    }

    public URI getWebPage() {
        return webPage;
    }

    public void setWebPage(URI webPage) {
        this.webPage = webPage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NvaProject that)) {
            return false;
        }
        return Objects.equals(getContext(), that.getContext())
            && Objects.equals(getId(), that.getId())
            && Objects.equals(getType(), that.getType())
            && Objects.equals(getIdentifiers(), that.getIdentifiers())
            && Objects.equals(getTitle(), that.getTitle())
            && Objects.equals(getLanguage(), that.getLanguage())
            && Objects.equals(getAlternativeTitles(), that.getAlternativeTitles())
            && Objects.equals(getStartDate(), that.getStartDate())
            && Objects.equals(getEndDate(), that.getEndDate())
            && Objects.equals(getFunding(), that.getFunding())
            && Objects.equals(getCoordinatingInstitution(), that.getCoordinatingInstitution())
            && Objects.equals(getContributors(), that.getContributors())
            && getStatus() == that.getStatus()
            && Objects.equals(getAcademicSummary(), that.getAcademicSummary())
            && Objects.equals(getPopularScientificSummary(), that.getPopularScientificSummary())
            && Objects.equals(getPublished(), that.getPublished())
            && Objects.equals(getPublishable(), that.getPublishable())
            && Objects.equals(getCreated(), that.getCreated())
            && Objects.equals(getLastModified(), that.getLastModified())
            && Objects.equals(getContactInfo(), that.getContactInfo())
            && Objects.equals(getFundingAmount(), that.getFundingAmount())
            && Objects.equals(getMethod(), that.getMethod())
            && Objects.equals(getEquipment(), that.getEquipment())
            && Objects.equals(getProjectCategories(), that.getProjectCategories())
            && Objects.equals(getKeywords(), that.getKeywords())
            && Objects.equals(getExternalSources(), that.getExternalSources())
            && Objects.equals(getRelatedProjects(), that.getRelatedProjects())
            && Objects.equals(getInstitutionsResponsibleForResearch(), that.getInstitutionsResponsibleForResearch())
            && Objects.equals(getHealthProjectData(), that.getHealthProjectData())
            && Objects.equals(getApprovals(), that.getApprovals())
            && Objects.equals(getExemptFromPublicDisclosure(), that.getExemptFromPublicDisclosure())
            && Objects.equals(getCreator(), that.getCreator())
            && Objects.equals(getWebPage(), that.getWebPage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContext(), getId(), getType(), getIdentifiers(), getTitle(), getLanguage(),
            getAlternativeTitles(), getStartDate(), getEndDate(), getFunding(),
            getCoordinatingInstitution(), getContributors(), getStatus(), getAcademicSummary(),
            getPopularScientificSummary(), getPublished(), getPublishable(), getCreated(),
            getLastModified(), getContactInfo(), getFundingAmount(), getMethod(), getEquipment(),
            getProjectCategories(), getKeywords(), getExternalSources(), getRelatedProjects(),
            getInstitutionsResponsibleForResearch(), getHealthProjectData(), getApprovals(),
            getExemptFromPublicDisclosure(), getCreator(), getWebPage());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    public CristinProject toCristinProject() {
        return new CristinProjectBuilder().apply(this);
    }

    public static final class Builder {

        private final transient NvaProject nvaProject;

        public Builder() {
            nvaProject = new NvaProject();
        }

        public Builder withContext(String context) {
            nvaProject.setContext(context);
            return this;
        }

        public Builder withId(URI id) {
            nvaProject.setId(id);
            return this;
        }

        public Builder withType(String type) {
            nvaProject.setType(type);
            return this;
        }

        public Builder withIdentifiers(List<Map<String, String>> identifiers) {
            nvaProject.setIdentifiers(identifiers);
            return this;
        }

        public Builder withTitle(String title) {
            nvaProject.setTitle(title);
            return this;
        }

        public Builder withLanguage(URI language) {
            nvaProject.setLanguage(language);
            return this;
        }

        public Builder withAlternativeTitles(List<Map<String, String>> alternativeTitles) {
            nvaProject.setAlternativeTitles(alternativeTitles);
            return this;
        }

        public Builder withStartDate(Instant startDate) {
            nvaProject.setStartDate(startDate);
            return this;
        }

        public Builder withEndDate(Instant endDate) {
            nvaProject.setEndDate(endDate);
            return this;
        }

        public Builder withFunding(List<Funding> funding) {
            nvaProject.setFunding(funding);
            return this;
        }

        public Builder withCoordinatingInstitution(Organization coordinatingInstitution) {
            nvaProject.setCoordinatingInstitution(coordinatingInstitution);
            return this;
        }

        public Builder withContributors(List<NvaContributor> contributors) {
            nvaProject.setContributors(contributors);
            return this;
        }

        public Builder withStatus(ProjectStatus status) {
            nvaProject.setStatus(status);
            return this;
        }

        public Builder withAcademicSummary(Map<String, String> academicSummary) {
            nvaProject.setAcademicSummary(academicSummary);
            return this;
        }

        public Builder withPopularScientificSummary(Map<String, String> popularScientificSummary) {
            nvaProject.setPopularScientificSummary(popularScientificSummary);
            return this;
        }

        public Builder withPublished(Boolean published) {
            nvaProject.setPublished(published);
            return this;
        }

        public Builder withPublishable(Boolean publishable) {
            nvaProject.setPublishable(publishable);
            return this;
        }

        public Builder withCreated(DateInfo created) {
            nvaProject.setCreated(created);
            return this;
        }

        public Builder withLastModified(DateInfo lastModified) {
            nvaProject.setLastModified(lastModified);
            return this;
        }

        public Builder withContactInfo(ContactInfo contactInfo) {
            nvaProject.setContactInfo(contactInfo);
            return this;
        }

        public Builder withFundingAmount(FundingAmount fundingAmount) {
            nvaProject.setFundingAmount(fundingAmount);
            return this;
        }

        public Builder withMethod(Map<String, String> method) {
            nvaProject.setMethod(method);
            return this;
        }

        public Builder withEquipment(Map<String, String> equipment) {
            nvaProject.setEquipment(equipment);
            return this;
        }

        public Builder withProjectCategories(List<TypedLabel> projectCategories) {
            nvaProject.setProjectCategories(projectCategories);
            return this;
        }

        public Builder withKeywords(List<TypedLabel> keywords) {
            nvaProject.setKeywords(keywords);
            return this;
        }

        public Builder withExternalSources(List<ExternalSource> externalSources) {
            nvaProject.setExternalSources(externalSources);
            return this;
        }

        public Builder withRelatedProjects(List<URI> relatedProjects) {
            nvaProject.setRelatedProjects(relatedProjects);
            return this;
        }

        public Builder withInstitutionsResponsibleForResearch(List<Organization> institutionsResponsibleForResearch) {
            nvaProject.setInstitutionsResponsibleForResearch(institutionsResponsibleForResearch);
            return this;
        }

        public Builder withHealthProjectData(HealthProjectData healthProjectData) {
            nvaProject.setHealthProjectData(healthProjectData);
            return this;
        }

        public Builder withApprovals(List<Approval> approvals) {
            nvaProject.setApprovals(approvals);
            return this;
        }

        public Builder withExemptFromPublicDisclosure(Boolean exemptFromPublicDisclosure) {
            nvaProject.setExemptFromPublicDisclosure(exemptFromPublicDisclosure);
            return this;
        }

        public Builder withCreator(NvaContributor creator) {
            nvaProject.setCreator(creator);
            return this;
        }

        public Builder withWebPage(URI webPage) {
            nvaProject.setWebPage(webPage);
            return this;
        }

        public NvaProject build() {
            return nvaProject;
        }
    }
}
