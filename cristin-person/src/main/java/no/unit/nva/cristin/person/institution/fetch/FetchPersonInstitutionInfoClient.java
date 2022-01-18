package no.unit.nva.cristin.person.institution.fetch;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.INSTITUTION_PATH;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.person.model.cristin.CristinPersonInstitutionInfo;
import no.unit.nva.cristin.person.model.nva.PersonInstitutionInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.paths.UriWrapper;

public class FetchPersonInstitutionInfoClient extends ApiClient {

    /**
     * Create FetchPersonInstitutionInfoClient with default HTTP client.
     */
    public FetchPersonInstitutionInfoClient() {
        this(HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(30))
            .build());
    }

    public FetchPersonInstitutionInfoClient(HttpClient client) {
        super(client);
    }

    /**
     * Fetches Cristin data from upstream into response object serialized to client.
     */
    public PersonInstitutionInfo generateGetResponse(String personId, String institutionId) throws ApiGatewayException {
        return fetchCristinData(personId, institutionId).toPersonInstitutionInfo();
    }

    private CristinPersonInstitutionInfo fetchCristinData(String personId, String orgId)
        throws ApiGatewayException {

        URI cristinUri = generateCristinUri(personId, orgId);
        HttpResponse<String> response = fetchGetResult(cristinUri);
        URI idUri = generateIdUri(personId, orgId);
        checkHttpStatusCode(idUri, response.statusCode());

        return getDeserializedResponse(response, CristinPersonInstitutionInfo.class);
    }

    private URI generateCristinUri(String personId, String orgId) {
        return new UriWrapper(CRISTIN_API_URL)
            .addChild(PERSON_PATH).addChild(personId)
            .addChild(INSTITUTION_PATH).addChild(orgId)
            .getUri();
    }

    private URI generateIdUri(String personId, String orgId) {
        return new UriWrapper(HTTPS, DOMAIN_NAME)
            .addChild(PERSON_PATH_NVA).addChild(personId)
            .addChild(ORGANIZATION_PATH).addChild(orgId)
            .getUri();
    }
}
