package no.unit.nva.cristin.person.client;

import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_IDENTIFIER_NOT_FOUND_FOR_URI;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.person.client.CristinPersonQuery.CRISTIN_API_PERSONS_PATH;
import static no.unit.nva.utils.UriUtils.PERSON;

public class AuthorizedCristinPersonApiClient extends CristinPersonApiClient {

    public static final String NATIONAL_ID = "national_id";

    public AuthorizedCristinPersonApiClient(HttpClient client) {
        super(client);
    }


    /**
     * Fetch CristinPerson by national identification number (nationalIdentifier).
     * Request against Cristin API must be authenticated
     *
     * @param nationalIdentificationNumber valid national identification number - 11 digits
     * @return CristinPerson without national identification number
     * @throws ApiGatewayException when upstream problems
     */
    @Override
    public CristinPerson getCristinPerson(String nationalIdentificationNumber) throws ApiGatewayException {
        URI uri = fromNationalIdentificationNumber(nationalIdentificationNumber);
        HttpResponse<String> response = fetchQueryResults(uri);
        checkHttpStatusCode(UriUtils.getNvaApiId(nationalIdentificationNumber, PERSON), response.statusCode());
        List<CristinPerson> personsFromQuery = asList(getDeserializedResponse(response, CristinPerson[].class));
        if (personsFromQuery.isEmpty()) {
            throw new NotFoundException(String.format(ERROR_MESSAGE_IDENTIFIER_NOT_FOUND_FOR_URI, nationalIdentificationNumber));
        }
        return super.getCristinPerson(personsFromQuery.get(0).getCristinPersonId());
    }

    private URI fromNationalIdentificationNumber(String nationalIdentifier) {
        return new UriWrapper(CRISTIN_API_URL)
                .addChild(CRISTIN_API_PERSONS_PATH)
                .addQueryParameter(NATIONAL_ID, nationalIdentifier)
                .getUri();
    }
}
