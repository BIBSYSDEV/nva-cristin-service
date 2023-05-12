package no.unit.nva.cristin.organization;

import java.util.Map;
import no.unit.nva.cristin.model.SearchResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface IQueryApiClient<T> {

    SearchResponse<T> executeQuery(Map<String, String> queryParams) throws ApiGatewayException;

}
