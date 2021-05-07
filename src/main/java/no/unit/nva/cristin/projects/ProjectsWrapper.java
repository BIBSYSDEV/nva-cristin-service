package no.unit.nva.cristin.projects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static no.unit.nva.cristin.projects.Constants.BASE_URL;
import static no.unit.nva.cristin.projects.Constants.PROJECT_SEARCH_CONTEXT_URL;
import static no.unit.nva.cristin.projects.Constants.QUESTION_MARK;
import static no.unit.nva.cristin.projects.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.projects.JsonPropertyNames.FIRST_RECORD;
import static no.unit.nva.cristin.projects.JsonPropertyNames.HITS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.NEXT_RESULTS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.PROCESSING_TIME;
import static no.unit.nva.cristin.projects.JsonPropertyNames.SEARCH_STRING;
import static no.unit.nva.cristin.projects.JsonPropertyNames.SIZE;
import static no.unit.nva.cristin.projects.UriUtils.buildUri;
import static no.unit.nva.cristin.projects.UriUtils.queryParameters;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonInclude(ALWAYS)
@JsonPropertyOrder({CONTEXT, ID, SIZE, SEARCH_STRING, PROCESSING_TIME, FIRST_RECORD, NEXT_RESULTS, HITS})
public class ProjectsWrapper {

    @JsonProperty("@context")
    private String context = PROJECT_SEARCH_CONTEXT_URL;
    @JsonProperty
    private URI id;
    @JsonProperty
    private Integer size;
    @JsonProperty
    private String searchString;
    @JsonProperty
    private Long processingTime;
    @JsonProperty
    private Integer firstRecord;
    @JsonProperty
    private String nextResults; // TODO: NP-2385: Add previous results as well
    @JsonProperty
    private List<NvaProject> hits;

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

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public Long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }

    public Integer getFirstRecord() {
        return firstRecord;
    }

    public void setFirstRecord(Integer firstRecord) {
        this.firstRecord = firstRecord;
    }

    public String getNextResults() {
        return nextResults;
    }

    public void setNextResults(String nextResults) {
        this.nextResults = nextResults;
    }

    public List<NvaProject> getHits() {
        return hits;
    }

    public void setHits(List<NvaProject> hits) {
        this.hits = hits;
    }

    /**
     * Assigns values to some of the fields using supplied query parameters.
     *
     * @param queryParams the query params
     * @return ProjectsWrapper object with field values from query parameters
     */
    public ProjectsWrapper usingQueryParams(Map<String, String> queryParams) {
        String queryParamsAsString = queryParameters(queryParams);
        this.id = buildUri(BASE_URL, QUESTION_MARK + queryParamsAsString);
        this.searchString = queryParamsAsString;
        return this;
    }

    // TODO: NP-2385

    /**
     * Assigns values to some of the fields using supplied http headers.
     *
     * @param headers the headers
     * @return ProjectsWrapper object with field values from http headers
     */
    public ProjectsWrapper usingHeaders(HttpHeaders headers) {
        this.size = 0;
        this.firstRecord = 0;
        //this.nextResults = null;
        return this;
    }

    public ProjectsWrapper withProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
        return this;
    }

    public ProjectsWrapper withHits(List<NvaProject> hits) {
        this.hits = hits;
        return this;
    }
}
