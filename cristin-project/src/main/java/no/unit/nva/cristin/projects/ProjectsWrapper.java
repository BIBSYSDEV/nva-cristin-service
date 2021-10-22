package no.unit.nva.cristin.projects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static no.unit.nva.cristin.projects.Constants.LINK;
import static no.unit.nva.cristin.projects.Constants.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.projects.Constants.PAGE;
import static no.unit.nva.cristin.projects.Constants.PROJECT_SEARCH_CONTEXT_URL;
import static no.unit.nva.cristin.projects.Constants.REL_NEXT;
import static no.unit.nva.cristin.projects.Constants.REL_PREV;
import static no.unit.nva.cristin.projects.Constants.X_TOTAL_COUNT;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_PAGE_OUT_OF_SCOPE;
import static no.unit.nva.cristin.projects.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.projects.JsonPropertyNames.FIRST_RECORD;
import static no.unit.nva.cristin.projects.JsonPropertyNames.HITS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.NEXT_RESULTS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.PREVIOUS_RESULTS;
import static no.unit.nva.cristin.projects.JsonPropertyNames.PROCESSING_TIME;
import static no.unit.nva.cristin.projects.JsonPropertyNames.SEARCH_STRING;
import static no.unit.nva.cristin.projects.JsonPropertyNames.SIZE;
import static no.unit.nva.cristin.projects.UriUtils.getNvaProjectUriWithParams;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.JacocoGenerated;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonInclude(ALWAYS)
@JsonPropertyOrder({CONTEXT, ID, SIZE, SEARCH_STRING, PROCESSING_TIME, FIRST_RECORD, NEXT_RESULTS, PREVIOUS_RESULTS,
    HITS})
public class ProjectsWrapper {

    @JsonIgnore
    public static final int FIRST_RECORD_ZERO_WHEN_NO_HITS = 0;

    @JsonProperty("@context")
    private String context = PROJECT_SEARCH_CONTEXT_URL;
    @JsonProperty
    private URI id;
    @JsonProperty
    private Integer size;
    @JsonProperty
    private Long processingTime;
    @JsonProperty
    private Integer firstRecord;
    @JsonProperty
    private URI nextResults;
    @JsonProperty
    private URI previousResults;
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

    @JsonProperty
    public String getSearchString() {
        return id.getQuery();
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

    public URI getNextResults() {
        return nextResults;
    }

    public void setNextResults(URI nextResults) {
        this.nextResults = nextResults;
    }

    public URI getPreviousResults() {
        return previousResults;
    }

    public void setPreviousResults(URI previousResults) {
        this.previousResults = previousResults;
    }

    public List<NvaProject> getHits() {
        return hits;
    }

    public void setHits(List<NvaProject> hits) {
        this.hits = hits;
    }

    /**
     * Assigns value to some field values using supplied headers and query parameters.
     *
     * @param headers     the headers from response
     * @param queryParams the query params from request
     * @return ProjectsWrapper object with some field values set using the supplied parameters
     * @throws BadRequestException if page requested is invalid
     */
    public ProjectsWrapper usingHeadersAndQueryParams(HttpHeaders headers, Map<String, String> queryParams)
        throws BadRequestException {

        this.size = getSizeHeader(headers);
        this.id = getNvaProjectUriWithParams(queryParams);
        this.firstRecord = this.size > 0 ? indexOfFirstEntryInPageCalculatedFromParams(queryParams) :
            FIRST_RECORD_ZERO_WHEN_NO_HITS;

        int currentPage = Integer.parseInt(queryParams.get(PAGE));

        if (outOfScope(currentPage)) {
            throw new BadRequestException(String.format(ERROR_MESSAGE_PAGE_OUT_OF_SCOPE, this.size));
        }

        String linkHeader = headers.firstValue(LINK).orElse(EMPTY_STRING);

        if (linkHeader.contains(REL_NEXT) && matchesCriteriaForNextRel(queryParams)) {
            this.nextResults = generateIdUriWithPageFromParams(currentPage + 1, queryParams);
        }

        if (linkHeader.contains(REL_PREV) && matchesCriteriaForPrevRel(currentPage)) {
            this.previousResults = generateIdUriWithPageFromParams(currentPage - 1, queryParams);
        }

        return this;
    }

    private boolean outOfScope(int currentPage) {
        return this.size < this.firstRecord || this.size == 0 && currentPage > 1;
    }

    private boolean matchesCriteriaForPrevRel(int currentPage) {
        return currentPage > 1;
    }

    private boolean matchesCriteriaForNextRel(Map<String, String> queryParams) {
        return this.size >= this.firstRecord + Integer.parseInt(queryParams.get(NUMBER_OF_RESULTS));
    }

    private URI generateIdUriWithPageFromParams(int newPage, Map<String, String> queryParams) {
        Map<String, String> newParams = new ConcurrentHashMap<>(queryParams);
        newParams.put(PAGE, String.valueOf(newPage));
        return getNvaProjectUriWithParams(newParams);
    }

    private Integer indexOfFirstEntryInPageCalculatedFromParams(Map<String, String> queryParams) {
        int page = Integer.parseInt(queryParams.get(PAGE));
        int numberOfResults = Integer.parseInt(queryParams.get(NUMBER_OF_RESULTS));

        return (page - 1) * numberOfResults + 1;
    }

    private int getSizeHeader(HttpHeaders headers) {
        return (int) headers.firstValueAsLong(X_TOTAL_COUNT).orElse(0);
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
