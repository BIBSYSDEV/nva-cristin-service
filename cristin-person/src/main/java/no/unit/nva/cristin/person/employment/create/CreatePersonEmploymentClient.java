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
import no.unit.nva.cristin.person.model.nva.Employment;
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
     * @param identifier Identifier of Person to add employment to
     * @param employment Request object to serialize and send to upstream
     * @param cristinInstitutionNumber First number sequence of a Cristin organization identifier
     * @return the response from upstream deserialized to object
     * @throws ApiGatewayException if something went wrong and that can be returned to client
     */
    public Employment createEmploymentInCristin(String identifier, Employment employment,
                                                String cristinInstitutionNumber)
        throws ApiGatewayException {

        var payload = generatePayloadFromRequest(employment);
        var uri = getCristinPostUri(identifier);
        var response = post(uri, payload, cristinInstitutionNumber);
        checkPostHttpStatusCode(getNvaPostUri(identifier), response.statusCode(), response.body());
        var deserializedResponse = deserializeResponse(response);

        return deserializedResponse.toEmployment(identifier);
    }

    private String generatePayloadFromRequest(Employment employment) {
        return attempt(() -> OBJECT_MAPPER.writeValueAsString(employment.toCristinEmployment()))
                   .orElseThrow();
    }

    private URI getCristinPostUri(String identifier) {
        return UriWrapper.fromUri(getCristinUri(identifier, PERSON_PATH)).addChild(EMPLOYMENT_PATH_CRISTIN).getUri();
    }

    private URI getNvaPostUri(String identifier) {
        return UriWrapper.fromUri(getNvaApiId(identifier, PERSON_PATH_NVA)).addChild(EMPLOYMENT_PATH).getUri();
    }

    private CristinPersonEmployment deserializeResponse(HttpResponse<String> response) throws BadGatewayException {
        return getDeserializedResponse(response, CristinPersonEmployment.class);
    }
}
