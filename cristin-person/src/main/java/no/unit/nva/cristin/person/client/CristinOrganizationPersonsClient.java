package no.unit.nva.cristin.person.client;

import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
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
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;

public class CristinOrganizationPersonsClient extends CristinPersonApiClient {


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

        long startRequestTime = System.currentTimeMillis();
        HttpResponse<String> response = queryOrganizationPersons(new HashMap(requestQueryParams));
        List<CristinPerson> cristinPersons = getEnrichedPersonsUsingQueryResponse(response);
        List<Person> persons = mapCristinPersonsToNvaPersons(cristinPersons);
        long endRequestTime = System.currentTimeMillis();
        URI id = getServiceUri(requestQueryParams);
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
        URI uri = generateOragnizationPersonsUrl(parameters);
        HttpResponse<String> response = fetchQueryResults(uri);
        URI id = getServiceUri(parameters);
        checkHttpStatusCode(id, response.statusCode());

        return response;
    }

    private URI generateOragnizationPersonsUrl(Map<String, String> parameters) {
        return new CristinPersonQuery()
                .withFromPage(parameters.get(PAGE))
                .withItemsPerPage(parameters.get(NUMBER_OF_RESULTS))
                .withParentUnitId(parameters.get(IDENTIFIER)).toURI();
    }

    private URI getServiceUri(Map<String, String> queryParameters) {
        final String identifier = queryParameters.remove(IDENTIFIER);
        return new UriWrapper(HTTPS,
                DOMAIN_NAME).addChild(BASE_PATH)
                .addChild(ORGANIZATION_PATH)
                .addChild(identifier)
                .addChild(PERSONS_PATH)
                .addQueryParameters(queryParameters)
                .getUri();
    }
}
