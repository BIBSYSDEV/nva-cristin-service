package no.unit.nva.cristin.person.affiliations.client;

import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.person.affiliations.model.CristinPositionCode;
import no.unit.nva.cristin.person.affiliations.model.PositionCode;
import no.unit.nva.cristin.person.affiliations.model.PositionCodes;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import static no.unit.nva.utils.UriUtils.addLanguage;

public class CristinPositionCodesClient extends ApiClient {

    public static final String CONTEXT = "https://bibsysdev.github.io/src/position-context.json";
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
    public PositionCodes generateQueryResponse() throws ApiGatewayException {
        HttpResponse<String> response = fetchQueryResults(createUpstreamUri());
        checkHttpStatusCode(createIdUri(), response.statusCode());
        List<CristinPositionCode> cristinPositionCodes =
            asList(getDeserializedResponse(response, CristinPositionCode[].class));
        Set<PositionCode> positionCodes = mapPositionCodesToNva(cristinPositionCodes);

        return new PositionCodes(CONTEXT, positionCodes);
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

    private Set<PositionCode> mapPositionCodesToNva(List<CristinPositionCode> cristinPositionCodes) {
        return cristinPositionCodes.stream().map(CristinPositionCode::toPositionCode).collect(Collectors.toSet());
    }
}
