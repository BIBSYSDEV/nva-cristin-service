package no.unit.nva.cristin.person.client;

import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.PERSON_CONTEXT;
import static no.unit.nva.cristin.person.client.CristinPersonQuery.CRISTIN_API_PERSONS_PATH;
import static no.unit.nva.utils.UriUtils.PERSON;

public class AuthorizedCristinPersonApiClient extends CristinPersonApiClient {

    public static final String NATIONAL_ID = "national_id";

    public AuthorizedCristinPersonApiClient(HttpClient client) {
        super(client);
    }

    public AuthorizedCristinPersonApiClient() {
        super();
    }


    @Override
    public Person getPersonFromNationalIdentityNumber(String nationalIdentificationNumber) throws ApiGatewayException {
        URI uri = fromNationalIdentificationNumber(nationalIdentificationNumber);
        HttpResponse<String> queryResponse = fetchQueryResults(uri);
        checkHttpStatusCode(UriUtils.getNvaApiId(nationalIdentificationNumber, PERSON), queryResponse.statusCode());

        List<CristinPerson> cristinPersons =  asList(getDeserializedResponse(queryResponse, CristinPerson[].class));

        throwNotFoundIfNoMatches(cristinPersons);
        CristinPerson enrichedCristinPerson = enrichFirstMatchFromQueryResponse(cristinPersons);
        Person person = enrichedCristinPerson.toPerson();
        person.setContext(PERSON_CONTEXT);

        return person;


    }

    private URI fromNationalIdentificationNumber(String nationalIdentifier) {
        return new UriWrapper(CRISTIN_API_URL)
                .addChild(CRISTIN_API_PERSONS_PATH)
                .addQueryParameter(NATIONAL_ID, nationalIdentifier)
                .getUri();
    }
}
