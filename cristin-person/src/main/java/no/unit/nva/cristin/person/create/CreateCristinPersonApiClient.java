package no.unit.nva.cristin.person.create;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_CONTEXT;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import no.unit.nva.cristin.common.client.PostApiClient;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.paths.UriWrapper;

public class CreateCristinPersonApiClient extends PostApiClient {

    public CreateCristinPersonApiClient(HttpClient client) {
        super(client);
    }

    /**
     * Creates a person in Cristin from the supplied Person object.
     */
    public Person createPersonInCristin(Person person) throws ApiGatewayException {
        var payload = generatePayloadFromRequest(person);
        var uri = getCristinPersonPostUri();
        var response = post(uri, payload);
        checkPostHttpStatusCode(getNvaApiUri(PERSON_PATH_NVA), response.statusCode(), response.body());

        return createPersonFromResponse(response);
    }

    /**
     * Creates a person in Cristin from the supplied Person object and at allowed Cristin institution.
     */
    public Person createPersonInCristin(Person person, String cristinInstitutionNumber) throws ApiGatewayException {
        var payload = generatePayloadFromRequest(person);
        var uri = getCristinPersonPostUri();
        var response = post(uri, payload, cristinInstitutionNumber);
        checkPostHttpStatusCode(getNvaApiUri(PERSON_PATH_NVA), response.statusCode(), response.body());

        return createPersonFromResponse(response);
    }

    private Person createPersonFromResponse(HttpResponse<String> response) throws BadGatewayException {
        var responseCristinPerson = getDeserializedResponse(response, CristinPerson.class);

        return responseCristinPerson
                   .toPersonBuilderWithAuthorizedFields()
                   .withContext(PERSON_CONTEXT)
                   .build();
    }

    private String generatePayloadFromRequest(Person person) {
        return attempt(() -> OBJECT_MAPPER.writeValueAsString(person.toCristinPerson()))
                   .orElseThrow();
    }

    private URI getCristinPersonPostUri() {
        return  UriWrapper.fromUri(CRISTIN_API_URL).addChild(PERSON_PATH).getUri();
    }
}
