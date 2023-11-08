package no.unit.nva.cristin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.facet.Facet;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.google.common.net.HttpHeaders.LINK;
import static no.unit.nva.cristin.model.Constants.REL_NEXT;
import static no.unit.nva.cristin.model.Constants.REL_PREV;
import static no.unit.nva.cristin.model.Constants.X_TOTAL_COUNT;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static nva.commons.core.StringUtils.EMPTY_STRING;

@SuppressWarnings({"unused", "PMD.GodClass"})
@JacocoGenerated
@JsonInclude(ALWAYS)
@JsonPropertyOrder({
    JsonPropertyNames.CONTEXT, JsonPropertyNames.ID, JsonPropertyNames.SIZE, JsonPropertyNames.SEARCH_STRING,
    JsonPropertyNames.PROCESSING_TIME, JsonPropertyNames.FIRST_RECORD, JsonPropertyNames.NEXT_RESULTS,
    JsonPropertyNames.PREVIOUS_RESULTS, JsonPropertyNames.HITS, JsonPropertyNames.FACETS})
public class SearchResponse<E> implements JsonSerializable {

    @JsonIgnore
    public static final String ERROR_MESSAGE_PAGE_OUT_OF_SCOPE =
        "Page requested is out of scope. Query contains %s results";
    @JsonIgnore
    public static final int FIRST_RECORD_ZERO_WHEN_NO_HITS = 0;
    private static final String EMPTY_QUERY = null;

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
    private List<E> hits;
    @JsonProperty
    @JsonInclude(NON_NULL)
    private Map<String, List<Facet>> aggregations;

    private SearchResponse() {

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
        return Objects.nonNull(id) ? id.getQuery() : null;
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

    public List<E> getHits() {
        return hits;
    }

    public void setHits(List<E> hits) {
        this.hits = hits;
    }

    public Map<String, List<Facet>> getAggregations() {
        return aggregations;
    }

    public void setAggregations(Map<String, List<Facet>> aggregations) {
        this.aggregations = aggregations;
    }

    public SearchResponse<E> withContext(String context) {
        this.context = context;
        return this;
    }

    public SearchResponse<E> withProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
        return this;
    }

    public SearchResponse<E> withHits(List<E> hits) {
        this.hits = hits;
        return this;
    }

    public SearchResponse<E> withSize(int size) {
        this.size = size;
        return this;
    }

    public SearchResponse<E> withAggregations(Map<String, List<Facet>> aggregations) {
        this.aggregations = aggregations;
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
    public SearchResponse<E> usingHeadersAndQueryParams(HttpHeaders headers, Map<String, String> queryParams)
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
        UriWrapper newUri = new UriWrapper(id.getScheme(), id.getHost()).addChild(id.getPath());

        return newUri.addQueryParameters(newParams).getUri();
    }


    private Integer indexOfFirstEntryInPageCalculatedFromParams(Map<String, String> queryParams) {
        int page = Integer.parseInt(queryParams.get(PAGE));
        int numberOfResults = Integer.parseInt(queryParams.get(NUMBER_OF_RESULTS));

        return (page - 1) * numberOfResults + 1;
    }

    private int getSizeHeader(HttpHeaders headers) {
        return (int) headers.firstValueAsLong(X_TOTAL_COUNT).orElse(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchResponse<?> that)) {
            return false;
        }
        return Objects.equals(getContext(), that.getContext())
               && Objects.equals(getId(), that.getId())
               && Objects.equals(getSize(), that.getSize())
               && Objects.equals(getProcessingTime(), that.getProcessingTime())
               && Objects.equals(getFirstRecord(), that.getFirstRecord())
               && Objects.equals(getNextResults(), that.getNextResults())
               && Objects.equals(getPreviousResults(), that.getPreviousResults())
               && Objects.equals(getHits(), that.getHits())
               && Objects.equals(getAggregations(), that.getAggregations());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContext(),
                            getId(),
                            getSize(),
                            getProcessingTime(),
                            getFirstRecord(),
                            getNextResults(),
                            getPreviousResults(),
                            getHits(),
                            getAggregations());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @JsonIgnore
    public boolean isNotEmpty() {
        return !hits.isEmpty();
    }

}
