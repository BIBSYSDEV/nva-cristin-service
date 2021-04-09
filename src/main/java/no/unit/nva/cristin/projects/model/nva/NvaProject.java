package no.unit.nva.cristin.projects.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.projects.JsonPropertyNames.ALTERNATIVE_TITLES;
import static no.unit.nva.cristin.projects.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.projects.JsonPropertyNames.CONTRIBUTORS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.projects.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.GRANT;
import static no.unit.nva.cristin.projects.JsonPropertyNames.ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.IDENTIFIER;
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
@JsonInclude(NON_NULL) // TODO: NP-2424: Remove this when all fields have been initialized as empty instead of null
@JsonPropertyOrder({CONTEXT, ID, TYPE, IDENTIFIER, TITLE, LANGUAGE, ALTERNATIVE_TITLES, START_DATE, END_DATE,
    GRANT, COORDINATING_INSTITUTION, CONTRIBUTORS})
public class NvaProject {

    @JsonProperty(CONTEXT)
    @JsonInclude(NON_NULL)
    private String context;
    private URI id;
    private String type;
    @JsonPropertyOrder(alphabetic = true)
    private List<Map<String, String>> identifier;
    private String title;
    private URI language;
    @JsonPropertyOrder(alphabetic = true)
    private List<Map<String, String>> alternativeTitles;
    private Instant startDate;
    private Instant endDate;
    // TODO: NP-2155: Add Grant/Funding field later
    private NvaOrganization coordinatingInstitution;
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

    public List<Map<String, String>> getIdentifier() {
        return identifier;
    }

    public void setIdentifier(List<Map<String, String>> identifier) {
        this.identifier = identifier;
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
