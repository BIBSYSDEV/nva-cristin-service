package no.unit.nva.cristin.common.client;

import no.unit.nva.client.GenericApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface CristinQueryApiClient<T, R> extends GenericApiClient {

    /**
     * Execute a query to upstream using query params and return its response inside a wrapper class.
     *
     * @param params params to use for the query
     * @return search response from query inside a wrapper object mapped to our own datamodel
     */
    SearchResponse<R> executeQuery(T params) throws ApiGatewayException;

}
