package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.projects.JsonPropertyNames.FIRST_RECORD;
import static no.unit.nva.cristin.projects.JsonPropertyNames.HITS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.NEXT_RESULTS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.PROCESSING_TIME;
import static no.unit.nva.cristin.projects.JsonPropertyNames.SEARCH_STRING;
import static no.unit.nva.cristin.projects.JsonPropertyNames.SIZE;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.util.List;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
@JsonPropertyOrder({CONTEXT, ID, SIZE, SEARCH_STRING, PROCESSING_TIME, FIRST_RECORD, NEXT_RESULTS, HITS})
public class ProjectsWrapper {

    private static final String CONTEXT_VALUE = "https://example.org/search-api-context.json"; // TODO: Create
    // constants class

    @JsonProperty("@context")
    private String context = CONTEXT_VALUE;
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
    private Integer nextResults;
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

    public Integer getNextResults() {
        return nextResults;
    }

    public void setNextResults(Integer nextResults) {
        this.nextResults = nextResults;
    }

    public List<NvaProject> getHits() {
        return hits;
    }

    public void setHits(List<NvaProject> hits) {
        this.hits = hits;
    }
}
