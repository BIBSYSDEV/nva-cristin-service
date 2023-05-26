package no.unit.nva.cristin.organization;

import no.unit.nva.cristin.model.SearchResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface IQueryApiClient<T, R> {

    SearchResponse<R> executeQuery(T params) throws ApiGatewayException;

}
