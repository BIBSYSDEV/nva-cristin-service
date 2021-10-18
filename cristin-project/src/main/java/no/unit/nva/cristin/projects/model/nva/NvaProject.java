package no.unit.nva.cristin.projects.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.projects.JsonPropertyNames.ALTERNATIVE_TITLES;
import static no.unit.nva.cristin.projects.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.projects.JsonPropertyNames.CONTRIBUTORS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.projects.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.GRANTS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.IDENTIFIERS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.TITLE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.TYPE;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonInclude(ALWAYS)
@JsonPropertyOrder({CONTEXT, ID, TYPE, IDENTIFIERS, TITLE, LANGUAGE, ALTERNATIVE_TITLES, START_DATE, END_DATE,
    GRANTS, COORDINATING_INSTITUTION, CONTRIBUTORS})
public class NvaProject {

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
    // TODO: NP-2155: Populate Grant/Funding field later
    @JsonProperty
    private List<Object> grants;
    @JsonProperty
    private NvaOrganization coordinatingInstitution;
    @JsonProperty
    private List<NvaContributor> contributors;

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
        return identifiers;
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
        return alternativeTitles;
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

    public List<Object> getGrants() {
        return grants;
    }

    public void setGrants(List<Object> grants) {
        this.grants = grants;
    }

    public NvaOrganization getCoordinatingInstitution() {
        return coordinatingInstitution;
    }

    public void setCoordinatingInstitution(NvaOrganization coordinatingInstitution) {
        this.coordinatingInstitution = coordinatingInstitution;
    }

    public List<NvaContributor> getContributors() {
        return contributors;
    }

    public void setContributors(List<NvaContributor> contributors) {
        this.contributors = contributors;
    }
}
