package no.unit.nva.cristin.person.query.v2;

import static no.unit.nva.client.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.cristin.model.Constants.PERSON_QUERY_CONTEXT;
import static no.unit.nva.utils.UriUtils.createIdUriFromParams;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import no.unit.nva.client.ClientVersion;
import no.unit.nva.cristin.common.client.CristinAuthorizedQueryClient;
import no.unit.nva.cristin.facet.CristinFacetConverter;
import no.unit.nva.cristin.facet.CristinFacetUriParamAppender;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.cristin.CristinPersonSearchResponse;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;

public class QueryPersonWithFacetsClient extends CristinPersonApiClient
    implements ClientVersion, CristinAuthorizedQueryClient<Map<String, String>, Person> {

    public static final String FACETS_PATH = "facets";
    public static final String CLIENT_VERSION = "2023-11-03";

    public QueryPersonWithFacetsClient() {
        super(defaultHttpClient());
    }

    /**
     * Creates a SearchResponse based on fetch from Cristin upstream including search facets.
     *
     * @param requestQueryParams the query params from the request
     * @return SearchResponse object with hits and facets from upstream and metadata
     * @throws ApiGatewayException if something went wrong
     */
    @Override
    public SearchResponse<Person> executeQuery(Map<String, String> requestQueryParams)
        throws ApiGatewayException {

        var startRequestTime = System.currentTimeMillis();
        var response = queryPersons(requestQueryParams);
        var mappedResponse = deserializeResponse(response);
        var personsData = Arrays.asList(mappedResponse.data());
        var cristinPersons = enrichPersons(personsData);
        var persons = mapCristinPersonsToNvaPersons(cristinPersons);
        var endRequestTime = System.currentTimeMillis();
        var id = createIdUriFromParams(requestQueryParams, PERSON_PATH_NVA);
        var convertedFacets = new CristinFacetConverter(id)
                                  .convert(mappedResponse.facets())
                                  .getConverted();

        return new SearchResponse<Person>(id)
                   .withContext(PERSON_QUERY_CONTEXT)
                   .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime))
                   .usingHeadersAndQueryParams(response.headers(), requestQueryParams)
                   .withFacets(convertedFacets)
                   .withHits(persons);
    }

    /**
     * Returns the same data as the open data query. We do not support query with authorized data in this version as
     * facet search is meant only for open data for the time being
     */
    @Override
    public SearchResponse<Person> executeAuthorizedQuery(Map<String, String> requestQueryParams)
        throws ApiGatewayException {

        return executeQuery(requestQueryParams);
    }

    /**
     * Perform a query for Persons matching criteria in parameters. Appends facet parameters to query uri if present.
     *
     * @param parameters containing search criteria for Cristin Person API
     * @return HttpResponse containing CristinPersons matching search criteria in parameters
     * @throws ApiGatewayException request fails or contains errors
     */
    @Override
    public HttpResponse<String> queryPersons(Map<String, String> parameters) throws ApiGatewayException {
        var uri = generateQueryPersonsUrl(parameters);
        uri = appendFacetsToUri(parameters, uri);
        var response = fetchQueryResults(uri);
        var id = createIdUriFromParams(parameters, PERSON_PATH_NVA);
        checkHttpStatusCode(id, response.statusCode(), response.body());
        return response;
    }

    @Override
    public String getClientVersion() {
        return CLIENT_VERSION;
    }

    private static URI appendFacetsToUri(Map<String, String> parameters, URI cristinUri) {
        return new CristinFacetUriParamAppender(cristinUri, parameters)
                   .getAppendedUri()
                   .addChild(FACETS_PATH)
                   .getUri();
    }

    private CristinPersonSearchResponse deserializeResponse(HttpResponse<String> response) throws BadGatewayException {
        return getDeserializedResponse(response, CristinPersonSearchResponse.class);
    }

    /**
     * Enrich list of CristinPerson from query with more data by doing one extra lookup request per person.
     */
    private List<CristinPerson> enrichPersons(List<CristinPerson> cristinPersons) {
        var cristinUris = extractCristinUrisFromPersons(cristinPersons);
        var individualResponses = fetchQueryResultsOneByOne(cristinUris);
        var enrichedCristinPersons = mapResponsesToCristinPersons(individualResponses);

        return allPersonsWereEnriched(cristinPersons, enrichedCristinPersons)
                   ? enrichedCristinPersons
                   : combineResultsWithQueryInCaseEnrichmentFails(cristinPersons, enrichedCristinPersons);
    }

}
