package no.unit.nva.cristin.person.employment.update;

import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.cristin.person.employment.Constants.EMPLOYMENT_PATH;
import static no.unit.nva.cristin.person.employment.Constants.EMPLOYMENT_PATH_CRISTIN;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import no.unit.nva.cristin.common.client.PatchApiClient;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.paths.UriWrapper;

public class UpdatePersonEmploymentClient extends PatchApiClient {

    public UpdatePersonEmploymentClient(HttpClient client) {
        super(client);
    }

    /**
     * Updates a person employment in Cristin.
     *
     * @return null if update was successful
     * @throws ApiGatewayException if something went wrong that can be mapped to a client response
     */
    public Void updatePersonEmploymentInCristin(String personId, String employmentId, ObjectNode request, String instNr)
        throws ApiGatewayException {

        URI uri = generateCristinUri(personId, employmentId);
        HttpResponse<String> response = patch(uri, request.toString(), instNr);
        checkPatchHttpStatusCode(generateIdUri(personId, employmentId), response.statusCode());

        return null;
    }

    private URI generateCristinUri(String personId, String employmentId) {
        return UriWrapper.fromUri(CRISTIN_API_URL).addChild(PERSON_PATH).addChild(personId)
            .addChild(EMPLOYMENT_PATH_CRISTIN).addChild(employmentId).getUri();
    }

    private URI generateIdUri(String personId, String employmentId) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(PERSON_PATH_NVA).addChild(personId)
            .addChild(EMPLOYMENT_PATH).addChild(employmentId).getUri();
    }
}
