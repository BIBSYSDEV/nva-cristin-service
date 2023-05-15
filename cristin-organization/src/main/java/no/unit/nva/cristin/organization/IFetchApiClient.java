package no.unit.nva.cristin.organization;

import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface IFetchApiClient<T, R> {

    R executeFetch(T params) throws ApiGatewayException;

}
