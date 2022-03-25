package no.unit.nva.cristin.person.employment.delete;

import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.employment.query.QueryPersonEmploymentClient;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static nva.commons.core.attempt.Try.attempt;

public class DeletePersonEmploymentClient extends QueryPersonEmploymentClient {
    public static final int FIRST_HIT = 0;
    private static final int SINGLE_HIT = 1;
    public static final String EMPLOYMENT_NOT_FOUND = "Employment not found";
    public static final String PERSON_NOT_FOUND = "Person not found";
    public static final String PERSON_ID_NOT_UNIQUE = "PersonId not unique";

    /**
     * Create CristinPersonApiClient with default HTTP client.
     */
    public DeletePersonEmploymentClient() {
        this(HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build());
    }

    public DeletePersonEmploymentClient(HttpClient client) {
        super(client);
    }

    /**
     * Deletes a identified persons specified employment.
     * @param personId identification of person
     * @param employmentId identification of employment
     * @return empty response ig successful
     * @throws ApiGatewayException when service encounters problems or user is not authorized.
     */
    public Void deletePersonEmployment(String personId, String employmentId) throws ApiGatewayException {
        SearchResponse<CristinPersonEmployment> searchResponse =
                attempt(() -> generateQueryResponse(personId)).orElseThrow();
        validateResponse(searchResponse);
        CristinPersonEmployment cristinPersonEmployment =
                (CristinPersonEmployment) searchResponse.getHits().get(FIRST_HIT);
        if (searchResponse.getHits().isEmpty() || !employmentId.equals(cristinPersonEmployment.getId())) {
            throw new NotFoundException(EMPLOYMENT_NOT_FOUND);
        }
        URI cristinUri = generateCristinUri(personId, employmentId);
        HttpResponse<String> response = deleteResults(cristinUri);
        checkResponseForBadRequestIndicatingNotFoundIdentifier(response.statusCode());
        return null;
    }

    private URI generateCristinUri(String personId, String employmentId) {
        return new UriWrapper(CRISTIN_API_URL)
                .addChild(PERSON_PATH)
                .addChild(personId)
                .addChild(EMPLOYMENT_PATH_CRISTIN)
                .addChild(employmentId)
                .getUri();
    }

    private void validateResponse(SearchResponse<CristinPersonEmployment> response) throws ApiGatewayException {
        if (response.getHits().isEmpty()) {
            throw new NotFoundException(PERSON_NOT_FOUND);
        }
        if (SINGLE_HIT != response.getHits().size()) {
            throw new BadRequestException(PERSON_ID_NOT_UNIQUE);
        }
    }

    private HttpResponse<String> deleteResults(URI uri) throws ApiGatewayException {
        HttpRequest httpRequest = HttpRequest.newBuilder(uri).DELETE().build();
        return getSuccessfulResponseOrThrowException(httpRequest);
    }
}
