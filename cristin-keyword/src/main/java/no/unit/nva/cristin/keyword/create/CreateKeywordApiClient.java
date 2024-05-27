package no.unit.nva.cristin.keyword.create;

import no.unit.nva.client.GenericApiClient;
import no.unit.nva.cristin.keyword.model.nva.Keyword;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface CreateKeywordApiClient extends GenericApiClient {

    Keyword create(Keyword input) throws ApiGatewayException;

}
