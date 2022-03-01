package no.unit.nva.cristin.person.employment.query;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.paths.UriWrapper;

public class QueryPersonEmploymentClient extends ApiClient {

    public static final String EMPLOYMENT_PATH_CRISTIN = "affiliations";
    public static final String EMPLOYMENT_PATH = "employment";
    public static final String EMPLOYMENT_QUERY_CONTEXT = "https://example.org/person-employment-search-context.json";
    public static final String BAD_REQUEST_FROM_UPSTREAM = "Upstream returned Bad Request. This might occur if "
        + "person identifier is not found in upstream";

    public QueryPersonEmploymentClient(HttpClient client) {
        super(client);
    }

    /**
     * Fetches Cristin data from upstream into response object serialized to client.
     */
    public SearchResponse<CristinPersonEmployment> generateQueryResponse(String identifier) throws ApiGatewayException {
        long startRequestTime = System.currentTimeMillis();
        URI cristinUri = generateCristinUri(identifier);
        HttpResponse<String> response = fetchQueryResults(cristinUri);
        URI idUri = generateIdUri(identifier);
        checkResponseForBadRequestIndicatingNotFoundIdentifier(response.statusCode());
        checkHttpStatusCode(idUri, response.statusCode());
        long requestTime = calculateProcessingTime(startRequestTime, System.currentTimeMillis());
        List<CristinPersonEmployment> employments =
            asList(getDeserializedResponse(response, CristinPersonEmployment[].class));

        return new SearchResponse<CristinPersonEmployment>(idUri)
            .withContext(EMPLOYMENT_QUERY_CONTEXT)
            .withProcessingTime(requestTime)
            .withSize(employments.size())
            .withHits(employments);
    }

    private void checkResponseForBadRequestIndicatingNotFoundIdentifier(int statusCode) throws BadRequestException {
        if (statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new BadRequestException(BAD_REQUEST_FROM_UPSTREAM);
        }
    }

    private URI generateCristinUri(String personId) {
        return new UriWrapper(CRISTIN_API_URL)
            .addChild(PERSON_PATH).addChild(personId)
            .addChild(EMPLOYMENT_PATH_CRISTIN)
            .getUri();
    }

    private URI generateIdUri(String personId) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH)
            .addChild(PERSON_PATH_NVA).addChild(personId)
            .addChild(EMPLOYMENT_PATH)
            .getUri();
    }
}
