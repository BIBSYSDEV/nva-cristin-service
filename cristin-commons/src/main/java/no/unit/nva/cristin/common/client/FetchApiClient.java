package no.unit.nva.cristin.common.client;

import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface FetchApiClient<T, R> extends GenericApiClient {

    /**
     * Execute a fetch to upstream using query params and return its response wrapped in our own data model.
     *
     * @param params params to use for the fetch
     * @return response from fetch mapped to our own data model
     */
    R executeFetch(T params) throws ApiGatewayException;

}
