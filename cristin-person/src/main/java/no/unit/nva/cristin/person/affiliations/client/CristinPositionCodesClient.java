package no.unit.nva.cristin.person.affiliations.client;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.utils.UriUtils.addLanguage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.affiliations.model.CristinPositionCode;
import no.unit.nva.cristin.person.affiliations.model.PositionCode;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.paths.UriWrapper;

public class CristinPositionCodesClient extends ApiClient {

    public static final String CONTEXT = "https://example.org/ontology";
    public static final String AFFILIATIONS_POSITIONS = "affiliations/positions";

    /**
     * Create CristinPositionCodesClient with default HTTP client.
     */
    public CristinPositionCodesClient() {
        this(HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(30))
            .build());
    }

    public CristinPositionCodesClient(HttpClient client) {
        super(client);
    }

    /**
     * Generates the response to be returned to client.
     */
    public SearchResponse<PositionCode> generateQueryResponse() throws ApiGatewayException {
        long startTime = System.currentTimeMillis();
        HttpResponse<String> response = fetchQueryResults(createUpstreamUri());
        checkHttpStatusCode(createIdUri(), response.statusCode());
        List<CristinPositionCode> cristinPositionCodes =
            asList(getDeserializedResponse(response, CristinPositionCode[].class));
        List<PositionCode> positionCodes = mapPositionCodesToNva(cristinPositionCodes);
        long processingTime = calculateProcessingTime(startTime, System.currentTimeMillis());

        return new SearchResponse<PositionCode>(createIdUri())
            .withSize(positionCodes.size())
            .withContext(CONTEXT)
            .withProcessingTime(processingTime)
            .withHits(positionCodes);
    }

    private static URI createUpstreamUri() {
        URI uri = new UriWrapper(CRISTIN_API_URL)
            .addChild(PERSON_PATH)
            .addChild(AFFILIATIONS_POSITIONS)
            .getUri();

        return addLanguage(uri);
    }

    private static URI createIdUri() {
        return new UriWrapper(HTTPS, DOMAIN_NAME)
            .addChild(BASE_PATH)
            .addChild(PERSON_PATH_NVA)
            .addChild(AFFILIATIONS_POSITIONS)
            .getUri();
    }

    private List<PositionCode> mapPositionCodesToNva(List<CristinPositionCode> cristinPositionCodes) {
        return cristinPositionCodes.stream().map(CristinPositionCode::toPositionCode).collect(Collectors.toList());
    }
}
