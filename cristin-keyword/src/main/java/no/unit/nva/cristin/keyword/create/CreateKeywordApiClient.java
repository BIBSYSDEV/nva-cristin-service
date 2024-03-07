package no.unit.nva.cristin.keyword.create;

import no.unit.nva.client.GenericApiClient;
import no.unit.nva.model.TypedLabel;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public interface CreateKeywordApiClient extends GenericApiClient {

    TypedLabel create(TypedLabel input) throws ApiGatewayException;

}
