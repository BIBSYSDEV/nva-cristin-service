package no.unit.nva.cristin.common.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static no.unit.nva.cristin.common.model.Constants.LINK;
import static no.unit.nva.cristin.common.model.Constants.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.common.model.Constants.PAGE;
import static no.unit.nva.cristin.common.model.Constants.REL_NEXT;
import static no.unit.nva.cristin.common.model.Constants.REL_PREV;
import static no.unit.nva.cristin.common.model.Constants.X_TOTAL_COUNT;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.cristin.common.util.UriUtils;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonSerializable;

@SuppressWarnings("unused")
@JacocoGenerated
@JsonInclude(ALWAYS)
@JsonPropertyOrder({
    JsonPropertyNames.CONTEXT, JsonPropertyNames.ID, JsonPropertyNames.SIZE, JsonPropertyNames.SEARCH_STRING,
    JsonPropertyNames.PROCESSING_TIME, JsonPropertyNames.FIRST_RECORD, JsonPropertyNames.NEXT_RESULTS,
    JsonPropertyNames.PREVIOUS_RESULTS, JsonPropertyNames.HITS})
public class SearchResponse implements JsonSerializable {

    @JsonIgnore
    public static final String ERROR_MESSAGE_PAGE_OUT_OF_SCOPE =
        "Page requested is out of scope. Query contains %s results";
    @JsonIgnore
    public static final int FIRST_RECORD_ZERO_WHEN_NO_HITS = 0;

    @JsonProperty("@context")
    private String context;
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
    private Set<?> hits;

    public SearchResponse() {

    }

    public SearchResponse(URI id) {
        this.id = id;
    }

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

    public Set<?> getHits() {
        return Objects.nonNull(hits) ? hits : Collections.emptySet();
    }

    public void setHits(Set<?> hits) {
        this.hits = hits;
    }

    public SearchResponse withContext(String context) {
        this.context = context;
        return this;
    }

    public SearchResponse withProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
        return this;
    }

    public SearchResponse withHits(Set<?> hits) {
        this.hits = hits;
        return this;
    }

    /**
     * Assigns value to some field values using supplied headers and query parameters.
     *
     * @param headers     the headers from response
     * @param queryParams the query params from request
     * @return SearchResponse object with some field values set using the supplied parameters
     * @throws BadRequestException if page requested is invalid
     */
    public SearchResponse usingHeadersAndQueryParams(HttpHeaders headers, Map<String, String> queryParams)
        throws BadRequestException {

        this.size = getSizeHeader(headers);
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
        return UriUtils.getUriFromOtherUriUsingNewParams(id, newParams);
    }

    private Integer indexOfFirstEntryInPageCalculatedFromParams(Map<String, String> queryParams) {
        int page = Integer.parseInt(queryParams.get(PAGE));
        int numberOfResults = Integer.parseInt(queryParams.get(NUMBER_OF_RESULTS));

        return (page - 1) * numberOfResults + 1;
    }

    private int getSizeHeader(HttpHeaders headers) {
        return (int) headers.firstValueAsLong(X_TOTAL_COUNT).orElse(0);
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchResponse)) {
            return false;
        }
        SearchResponse that = (SearchResponse) o;
        return Objects.equals(getContext(), that.getContext())
            && Objects.equals(getId(), that.getId())
            && Objects.equals(getSize(), that.getSize())
            && Objects.equals(getSearchString(), that.getSearchString())
            && Objects.equals(getProcessingTime(), that.getProcessingTime())
            && Objects.equals(getFirstRecord(), that.getFirstRecord())
            && Objects.equals(getNextResults(), that.getNextResults())
            && Objects.equals(getPreviousResults(), that.getPreviousResults())
            && getHits().equals(that.getHits());
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getContext(), getId(), getSize(), getSearchString(), getProcessingTime(), getFirstRecord(),
            getNextResults(), getPreviousResults(), getHits());
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
