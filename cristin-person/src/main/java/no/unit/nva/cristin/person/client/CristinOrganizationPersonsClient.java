package no.unit.nva.cristin.person.client;

import java.net.http.HttpClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PERSONS_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_CONTEXT;
import static no.unit.nva.cristin.model.Constants.SORT;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;

public class CristinOrganizationPersonsClient extends CristinPersonApiClient {

    public CristinOrganizationPersonsClient() {
        super();
    }

    public CristinOrganizationPersonsClient(HttpClient client) {
        super(client);
    }

    /**
     * Creates a SearchResponse based on fetch from Cristin upstream.
     *
     * @param requestQueryParams the query params from the request
     * @return SearchResponse object with hits from upstream and metadata
     * @throws ApiGatewayException if something went wrong
     */
    @Override
    public SearchResponse<Person> generateQueryResponse(Map<String, String> requestQueryParams)
            throws ApiGatewayException {

        var startRequestTime = System.currentTimeMillis();
        var response = queryOrganizationPersons(new HashMap<>(requestQueryParams));
        var cristinPersons = getEnrichedPersonsUsingQueryResponse(response);
        var persons = mapCristinPersonsToNvaPersons(cristinPersons);
        return getPersonSearchResponse(requestQueryParams, startRequestTime, response, persons);
    }

    /**
     * Creates a SearchResponse based on fetch from Cristin upstream for authorized User.
     *
     * @param requestQueryParams the query params from the request
     * @return SearchResponse object with hits from upstream and metadata containing NIN
     * @throws ApiGatewayException if something went wrong
     */
    @Override
    public SearchResponse<Person> authorizedGenerateQueryResponse(Map<String, String> requestQueryParams)
            throws ApiGatewayException {

        var startRequestTime = System.currentTimeMillis();
        var response = authorizedQueryOrganizationPersons(new HashMap<>(requestQueryParams));
        var cristinPersons = getEnrichedPersonsWithNinUsingQueryResponse(response);
        var personsWithAuthorizedFields = authorizedMappingCristinPersonsToNvaPersons(cristinPersons);
        return getPersonSearchResponse(requestQueryParams, startRequestTime, response, personsWithAuthorizedFields);
    }

    private SearchResponse<Person> getPersonSearchResponse(Map<String, String> requestQueryParams,
                                                           long startRequestTime,
                                                           HttpResponse<String> response,
                                                           List<Person> persons)
            throws nva.commons.apigateway.exceptions.BadRequestException {
        var endRequestTime = System.currentTimeMillis();
        var id = getServiceUri(requestQueryParams);
        return new SearchResponse<Person>(id)
                .withContext(PERSON_CONTEXT)
                .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime))
                .usingHeadersAndQueryParams(response.headers(), requestQueryParams)
                .withHits(persons);
    }


    /**
     * Perform a query for Persons matching criteria in parameters.
     *
     * @param parameters containing search criteria for Cristin Person API
     * @return HttpResponse containing CristinPersons matching search criteria in parameters
     * @throws ApiGatewayException request fails or contains errors
     */
    private HttpResponse<String> queryOrganizationPersons(Map<String, String> parameters) throws ApiGatewayException {
        var uri = generateOrganizationPersonsUrl(parameters);
        var response = fetchQueryResults(uri);
        checkHttpStatusCode(getServiceUri(parameters), response.statusCode(), response.body());
        return response;
    }

    private HttpResponse<String> authorizedQueryOrganizationPersons(Map<String, String> parameters)
            throws ApiGatewayException {
        var uri = generateOrganizationPersonsUrl(parameters);
        var response = fetchGetResultWithAuthentication(uri);
        checkHttpStatusCode(getServiceUri(parameters), response.statusCode(), response.body());
        return response;
    }

    private URI generateOrganizationPersonsUrl(Map<String, String> parameters) {
        var query = new CristinPersonQuery()
                                       .withFromPage(parameters.get(PAGE))
                                       .withItemsPerPage(parameters.get(NUMBER_OF_RESULTS))
                                       .withParentUnitId(parameters.get(IDENTIFIER));
        if (parameters.containsKey(NAME)) {
            query.withName(parameters.get(NAME));
        }
        if (parameters.containsKey(SORT)) {
            query.withSort(parameters.get(SORT));
        }
        return query.toURI();
    }

    private URI getServiceUri(Map<String, String> queryParameters) {
        var identifier = queryParameters.remove(IDENTIFIER);
        return new UriWrapper(HTTPS,
                DOMAIN_NAME).addChild(BASE_PATH)
                .addChild(ORGANIZATION_PATH)
                .addChild(identifier)
                .addChild(PERSONS_PATH)
                .addQueryParameters(queryParameters)
                .getUri();
    }
}
