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
import no.unit.nva.cristin.person.model.cristin.CristinPersonPost;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.paths.UriWrapper;

public class CreateCristinPersonApiClient extends PostApiClient {

    public CreateCristinPersonApiClient(HttpClient client) {
        super(client);
    }

    /**
     * Used for creating a person in Cristin from the supplied Person object.
     */
    public Person createPersonInCristin(Person person) throws ApiGatewayException {
        String payload = generatePayloadFromRequest(person);
        URI uri = getCristinPersonPostUri();
        HttpResponse<String> response = post(uri, payload);
        checkPostHttpStatusCode(getNvaApiUri(PERSON_PATH_NVA), response.statusCode());

        return createPersonFromResponse(response);
    }

    private Person createPersonFromResponse(HttpResponse<String> response) throws BadGatewayException {
        CristinPerson responseCristinPerson = getDeserializedResponse(response, CristinPerson.class);
        Person createdPerson = responseCristinPerson.toPerson();
        createdPerson.setContext(PERSON_CONTEXT);
        return createdPerson;
    }

    private String generatePayloadFromRequest(Person person) {
        CristinPersonPost requestCristinPerson = person.toCristinPersonPost();
        return attempt(() -> OBJECT_MAPPER.writeValueAsString(requestCristinPerson)).orElseThrow();
    }

    private URI getCristinPersonPostUri() {
        return new UriWrapper(CRISTIN_API_URL).addChild(PERSON_PATH).getUri();
    }
}
