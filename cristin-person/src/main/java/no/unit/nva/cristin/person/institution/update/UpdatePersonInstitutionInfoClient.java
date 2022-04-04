package no.unit.nva.cristin.person.institution.update;

import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.INSTITUTION_PATH;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import no.unit.nva.cristin.common.client.PatchApiClient;
import no.unit.nva.cristin.person.model.nva.PersonInstInfoPatch;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.paths.UriWrapper;

public class UpdatePersonInstitutionInfoClient extends PatchApiClient {

    public UpdatePersonInstitutionInfoClient(HttpClient client) {
        super(client);
    }

    /**
     * Updates a persons information related to supplied institution.
     *
     * @return An empty json if update was successful
     * @throws ApiGatewayException if something went wrong that can be mapped to a client response
     */
    public String updatePersonInstitutionInfoInCristin(String personId, String orgId, PersonInstInfoPatch request)
        throws ApiGatewayException {

        String payload = generatePayloadFromRequest(request);
        URI uri = generateCristinUri(personId, orgId);
        HttpResponse<String> response = patch(uri, payload);
        checkPatchHttpStatusCode(generateIdUri(personId, orgId), response.statusCode());

        return EMPTY_JSON;
    }

    private String generatePayloadFromRequest(PersonInstInfoPatch request) {
        return attempt(() -> OBJECT_MAPPER.writeValueAsString(request)).orElseThrow();
    }

    private URI generateCristinUri(String personId, String orgId) {
        return UriWrapper.fromUri(CRISTIN_API_URL)
            .addChild(PERSON_PATH).addChild(personId)
            .addChild(INSTITUTION_PATH).addChild(orgId)
            .getUri();
    }

    private URI generateIdUri(String personId, String orgId) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH)
            .addChild(PERSON_PATH_NVA).addChild(personId)
            .addChild(ORGANIZATION_PATH).addChild(orgId)
            .getUri();
    }
}
