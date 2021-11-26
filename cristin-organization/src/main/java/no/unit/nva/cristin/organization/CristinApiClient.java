package no.unit.nva.cristin.organization;

import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_BASE;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;


public class CristinApiClient {

    public static final String CRISTIN_PER_PAGE_PARAM = "per_page";
    private static final String CRISTIN_QUERY_NAME_PARAM = "name";
    private final transient HttpExecutorImpl httpExecutor;

    public CristinApiClient() {
        this(new HttpExecutorImpl());
    }

    public CristinApiClient(HttpExecutorImpl httpExecutor) {
        this.httpExecutor = httpExecutor;
    }

    /**
     * Get information for an Organization.
     *
     * @param uri the Cristin unit URI
     * @return an {@link Organization} containing the information
     * @throws NotFoundException when the URI does not correspond to an existing unit.
     */
    public Organization getOrganization(URI uri)
            throws NotFoundException, InterruptedException, FailedHttpRequestException {
        return httpExecutor.getOrganization(uri);
    }

    /**
     * Fetch Organizations matching given query criteria.
     *
     * @param requestQueryParams Map containing verified query parameters
     */
    public SearchResponse<Organization> queryOrganizations(Map<String, String> requestQueryParams)
            throws NotFoundException, FailedHttpRequestException {
        Map<String, String> cristinRequestQueryParams = translateToCristinApi(requestQueryParams);
        URI queryUri = new UriWrapper(HTTPS, CRISTIN_API_BASE)
                .addChild(UNITS_PATH)
                .addQueryParameters(cristinRequestQueryParams)
                .getUri();

        long start = System.currentTimeMillis();
        SearchResponse<Organization> searchResponse = httpExecutor.query(queryUri);

        return updateSearchResponseMetadata(searchResponse,requestQueryParams, System.currentTimeMillis() - start);
    }

    private Map<String, String> translateToCristinApi(Map<String, String> requestQueryParams) {
        return Map.of(
                CRISTIN_QUERY_NAME_PARAM, requestQueryParams.get(QUERY),
                PAGE, requestQueryParams.get(PAGE),
                CRISTIN_PER_PAGE_PARAM, requestQueryParams.get(NUMBER_OF_RESULTS));
    }

    private SearchResponse<Organization> updateSearchResponseMetadata(
            SearchResponse<Organization> searchResponse,
            Map<String, String> requestQueryParams,
            long timeUsed) {
        final URI baseUri = new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(ORGANIZATION_PATH).getUri();
        searchResponse.setId(new UriWrapper(baseUri).addQueryParameters(requestQueryParams).getUri());
        searchResponse.setFirstRecord(calculateFirstRecord(requestQueryParams));
        searchResponse.setNextResults(nextResult(baseUri, requestQueryParams, searchResponse.getSize()));
        searchResponse.setPreviousResults(previousResult(baseUri, requestQueryParams, searchResponse.getSize()));
        searchResponse.setProcessingTime(timeUsed);
        return searchResponse;
    }

    private URI previousResult(URI baseUri, Map<String, String> requestQueryParams, int totalSize) {
        int firstPage = Integer.parseInt(requestQueryParams.get(PAGE)) - 1;
        if (firstPage > 0 && totalSize > 0) {
            Map<String, String> nextMap = new ConcurrentHashMap<>(requestQueryParams);
            nextMap.put(PAGE, Integer.toString(firstPage));
            return new UriWrapper(baseUri).addQueryParameters(nextMap).getUri();
        }
        return null;
    }

    private URI nextResult(URI baseUri, Map<String, String> requestQueryParams, int totalSize) {
        int nextPage = Integer.parseInt(requestQueryParams.get(PAGE)) + 1;
        int pageSize = Integer.parseInt(requestQueryParams.get(NUMBER_OF_RESULTS));
        if (nextPage * pageSize < totalSize) {
            Map<String, String> nextMap = new ConcurrentHashMap<>(requestQueryParams);
            nextMap.put(PAGE, Integer.toString(nextPage));
            return new UriWrapper(baseUri).addQueryParameters(nextMap).getUri();
        }
        return null;
    }

    private Integer calculateFirstRecord(Map<String, String> requestQueryParams) {
        final int page = Integer.parseInt(requestQueryParams.get(PAGE));
        final int pageSize = Integer.parseInt(requestQueryParams.get(NUMBER_OF_RESULTS));
        return (page - 1) * pageSize + 1;
    }

}
