package no.unit.nva.cristin.projects.model.nva;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import no.unit.nva.cristin.model.nva.Organization;
import no.unit.nva.cristin.projects.ProjectStatus;
import nva.commons.core.JacocoGenerated;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.projects.JsonPropertyNames.ALTERNATIVE_TITLES;
import static no.unit.nva.cristin.projects.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.projects.JsonPropertyNames.CONTRIBUTORS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.projects.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.FUNDING;
import static no.unit.nva.cristin.projects.JsonPropertyNames.ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.IDENTIFIERS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.STATUS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.TITLE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import static no.unit.nva.cristin.projects.model.nva.NvaProject.NVA_ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.projects.model.nva.NvaProject.NVA_POPULAR_SCIENTIFIC_SUMMARY;

@JsonInclude(ALWAYS)
@JsonPropertyOrder({CONTEXT, ID, TYPE, IDENTIFIERS, TITLE, LANGUAGE, ALTERNATIVE_TITLES, START_DATE, END_DATE,
        FUNDING, COORDINATING_INSTITUTION, CONTRIBUTORS, STATUS, NVA_ACADEMIC_SUMMARY, NVA_POPULAR_SCIENTIFIC_SUMMARY})
public class NvaProject {

    public static final String NVA_ACADEMIC_SUMMARY = "academicSummary";
    public static final String NVA_POPULAR_SCIENTIFIC_SUMMARY = "popularScientificSummary";

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
    private Map<String, String>  academicSummary;
    @JsonProperty
    private Map<String, String>  popularScientificSummary;


    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

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


    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NvaProject)) {
            return false;
        }
        NvaProject that = (NvaProject) o;
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
                && Objects.equals(getContributors(), that.getContributors());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getContext(),
                getId(),
                getType(),
                getIdentifiers(),
                getTitle(),
                getLanguage(),
                getAlternativeTitles(),
                getStartDate(),
                getEndDate(),
                getFunding(),
                getCoordinatingInstitution(),
                getContributors());
    }

    @Override
    public String toString() {
        return "NvaProject{" +
                "context='" + context + '\'' +
                ", id=" + id +
                ", type='" + type + '\'' +
                ", identifiers=" + identifiers +
                ", title='" + title + '\'' +
                ", language=" + language +
                ", alternativeTitles=" + alternativeTitles +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", funding=" + funding +
                ", coordinatingInstitution=" + coordinatingInstitution +
                ", contributors=" + contributors +
                ", status=" + status +
                ", academicSummary=" + academicSummary +
                ", popularScientificSummary=" + popularScientificSummary +
                '}';
    }
}
