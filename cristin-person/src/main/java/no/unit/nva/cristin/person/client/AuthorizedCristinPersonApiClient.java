package no.unit.nva.cristin.person.client;

import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.person.client.CristinPersonQuery.CRISTIN_API_PERSONS_PATH;
import static no.unit.nva.utils.UriUtils.PERSON;
import static no.unit.nva.utils.UriUtils.addLanguage;

public class AuthorizedCristinPersonApiClient extends CristinPersonApiClient {


    public static final String NATIONAL_ID = "national_id";

    public AuthorizedCristinPersonApiClient(HttpClient client) {
        super(client);
    }


    /**
     * Fetch CristinPerson by national identification number (nationalIdentifier).
     * Request against Cristin API must be authenticated
     * @param nationalIdentifier valid national identification number - 11 digits
     * @return CristinPerson without national identification number
     * @throws ApiGatewayException when upstream problems
     */
    public CristinPerson getCristinPerson(String nationalIdentifier) throws ApiGatewayException {
        URI uri = fromNationalIdentifier(nationalIdentifier);
        HttpResponse<String> response = fetchGetResult(uri);
        checkHttpStatusCode(UriUtils.getNvaApiId(nationalIdentifier, PERSON), response.statusCode());
        return getDeserializedResponse(response, CristinPerson.class);
    }

    private URI fromNationalIdentifier(String nationalIdentifier) {
        URI uri = new UriWrapper(CRISTIN_API_URL)
                .addChild(CRISTIN_API_PERSONS_PATH)
                .addQueryParameter(NATIONAL_ID, nationalIdentifier)
                .getUri();
        return addLanguage(uri);
    }
}
