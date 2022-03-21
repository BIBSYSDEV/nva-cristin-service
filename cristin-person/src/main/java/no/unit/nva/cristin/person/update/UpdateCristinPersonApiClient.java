package no.unit.nva.cristin.person.update;

import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import no.unit.nva.cristin.common.client.PatchApiClient;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.paths.UriWrapper;

public class UpdateCristinPersonApiClient extends PatchApiClient {

    public UpdateCristinPersonApiClient(HttpClient client) {
        super(client);
    }

    /**
     * Updates a person in Cristin.
     *
     * @return An empty json if update was successful
     * @throws ApiGatewayException if something went wrong that can be mapped to a client response
     */
    public String updatePersonInCristin(String personId, ObjectNode request) throws ApiGatewayException {
        URI uri = generateCristinUri(personId);
        HttpResponse<String> response = patch(uri, request.toString());
        checkPatchHttpStatusCode(generateIdUri(personId), response.statusCode());

        return EMPTY_JSON;
    }

    private URI generateCristinUri(String personId) {
        return new UriWrapper(CRISTIN_API_URL).addChild(PERSON_PATH).addChild(personId).getUri();
    }

    private URI generateIdUri(String personId) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(PERSON_PATH_NVA).addChild(personId)
            .getUri();
    }
}
