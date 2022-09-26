package no.unit.nva.cristin.person.employment.delete;

import no.unit.nva.cristin.common.client.ApiClient;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.paths.UriWrapper;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.CRISTIN_INSTITUTION_HEADER;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.person.employment.Constants.EMPLOYMENT_PATH_CRISTIN;

public class DeletePersonEmploymentClient extends ApiClient {
    public static final String BAD_REQUEST_FROM_UPSTREAM = "Upstream returned Bad Request. This might occur if "
            + "person identifier or employment identifier are invalid";

    public static final String NOT_FOUND_REQUEST_FROM_UPSTREAM = "Upstream returned Not Found. This might occur if "
            + "person identifier or employment identifier are not found in upstream";


    public DeletePersonEmploymentClient(HttpClient client) {
        super(client);
    }

    /**
     * Deletes a identified persons specified employment.
     * @param personId identification of person
     * @param employmentId identification of employment
     * @param instNr first number sequence of a Cristin organization identifier
     * @return empty response if successful
     * @throws ApiGatewayException when service encounters problems or user is not authorized.
     */
    public Void deletePersonEmployment(String personId, String employmentId, String instNr) throws ApiGatewayException {
        URI cristinUri = generateCristinUri(personId, employmentId);
        HttpResponse<String> response = deleteResults(cristinUri, instNr);
        checkResponse(response.statusCode());
        return null;
    }

    private URI generateCristinUri(String personId, String employmentId) {
        return UriWrapper.fromUri(CRISTIN_API_URL)
                .addChild(PERSON_PATH)
                .addChild(personId)
                .addChild(EMPLOYMENT_PATH_CRISTIN)
                .addChild(employmentId)
                .getUri();
    }

    protected void checkResponse(int statusCode) throws ApiGatewayException {
        if (statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new BadRequestException(BAD_REQUEST_FROM_UPSTREAM);
        }
        if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new NotFoundException(NOT_FOUND_REQUEST_FROM_UPSTREAM);
        }
    }

    private HttpResponse<String> deleteResults(URI uri, String instNr) throws ApiGatewayException {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri)
                                      .header(CRISTIN_INSTITUTION_HEADER, instNr)
                                      .DELETE()
                                      .build();
        return getSuccessfulResponseOrThrowException(httpRequest);
    }
}
