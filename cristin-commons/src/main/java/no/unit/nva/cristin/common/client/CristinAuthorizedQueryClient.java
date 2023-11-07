package no.unit.nva.cristin.common.client;

import no.unit.nva.cristin.model.SearchResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface CristinAuthorizedQueryClient<T, R> extends CristinQueryApiClient<T, R> {

    /**
     * Execute an authorized query to upstream using query params and return its response inside a wrapper class.
     *
     * @param params params to use for the query
     * @return search response from query inside a wrapper object mapped to our own datamodel
     */
    SearchResponse<R> executeAuthorizedQuery(T params) throws ApiGatewayException;

}
