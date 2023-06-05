package no.unit.nva.cristin.common.client;

import no.unit.nva.cristin.model.SearchResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface QueryApiClient<T, R> {

    SearchResponse<R> executeQuery(T params) throws ApiGatewayException;

}
