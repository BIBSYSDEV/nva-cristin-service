package no.unit.nva.cristin.person.employment.create;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.cristin.person.employment.Constants.EMPLOYMENT_PATH;
import static no.unit.nva.cristin.person.employment.Constants.EMPLOYMENT_PATH_CRISTIN;
import static no.unit.nva.utils.UriUtils.getCristinUri;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import no.unit.nva.cristin.common.client.PostApiClient;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.paths.UriWrapper;

public class CreatePersonEmploymentClient extends PostApiClient {

    public CreatePersonEmploymentClient(HttpClient httpClient) {
        super(httpClient);
    }

    /**
     * Does a POST to upstream using parameters and returns response to client.
     *
     * @param identifier        Identifier of Person to add employment to
     * @param cristinEmployment Request object to serialize and send to upstream
     * @return the response from upstream deserialized to object
     * @throws ApiGatewayException if something went wrong and that can be returned to client
     */
    public CristinPersonEmployment createEmploymentInCristin(String identifier,
                                                             CristinPersonEmployment cristinEmployment)
        throws ApiGatewayException {

        String payload = generatePayloadFromRequest(cristinEmployment);
        URI uri = getCristinPostUri(identifier);
        HttpResponse<String> response = post(uri, payload);
        checkPostHttpStatusCode(getNvaPostUri(identifier), response.statusCode());

        return createPersonEmploymentFromResponse(response);
    }

    private String generatePayloadFromRequest(CristinPersonEmployment cristinEmployment) {
        return attempt(() -> OBJECT_MAPPER.writeValueAsString(cristinEmployment)).orElseThrow();
    }

    private URI getCristinPostUri(String identifier) {
        return new UriWrapper(getCristinUri(identifier, PERSON_PATH)).addChild(EMPLOYMENT_PATH_CRISTIN).getUri();
    }

    private URI getNvaPostUri(String identifier) {
        return new UriWrapper(getNvaApiId(identifier, PERSON_PATH_NVA)).addChild(EMPLOYMENT_PATH).getUri();
    }

    private CristinPersonEmployment createPersonEmploymentFromResponse(HttpResponse<String> response)
        throws BadGatewayException {

        return getDeserializedResponse(response, CristinPersonEmployment.class);
    }
}
