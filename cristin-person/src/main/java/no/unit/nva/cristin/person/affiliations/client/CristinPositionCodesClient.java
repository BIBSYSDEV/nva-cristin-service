package no.unit.nva.cristin.person.affiliations.client;

import static java.util.Arrays.asList;
import static no.unit.nva.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH;
import static no.unit.nva.cristin.model.Constants.PERSON_PATH_NVA;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.person.affiliations.model.CristinPositionCode;
import no.unit.nva.cristin.person.affiliations.model.PositionCode;
import no.unit.nva.cristin.person.affiliations.model.PositionCodes;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.paths.UriWrapper;

public class CristinPositionCodesClient extends ApiClient {

    public static final URI CONTEXT = URI.create("https://bibsysdev.github.io/src/position-context.json");
    public static final String AFFILIATIONS_POSITIONS = "affiliations/positions";

    /**
     * Create CristinPositionCodesClient with default HTTP client.
     */
    public CristinPositionCodesClient() {
        this(defaultHttpClient());
    }

    public CristinPositionCodesClient(HttpClient client) {
        super(client);
    }

    /**
     * Generates the response to be returned to client.
     *
     * @param positionStatus query param to indicate position status
     */
    public PositionCodes generateQueryResponse(Boolean positionStatus) throws ApiGatewayException {
        var response = fetchQueryResults(createUpstreamUri());
        checkHttpStatusCode(createIdUri(), response.statusCode(), response.body());
        var cristinPositionCodes =
            asList(getDeserializedResponse(response, CristinPositionCode[].class));
        cristinPositionCodes = filterPositionCodeIfRequested(cristinPositionCodes, positionStatus);
        var positionCodes = mapPositionCodesToNva(cristinPositionCodes);

        return new PositionCodes(CONTEXT, positionCodes);
    }

    private static URI createUpstreamUri() {
        return UriWrapper.fromUri(CRISTIN_API_URL)
            .addChild(PERSON_PATH)
            .addChild(AFFILIATIONS_POSITIONS)
            .getUri();
    }

    private static URI createIdUri() {
        return new UriWrapper(HTTPS, DOMAIN_NAME)
            .addChild(BASE_PATH)
            .addChild(PERSON_PATH_NVA)
            .addChild(AFFILIATIONS_POSITIONS)
            .getUri();
    }

    private List<CristinPositionCode> filterPositionCodeIfRequested(List<CristinPositionCode> cristinPositionCodes,
                                                                    Boolean positionStatus) {
        return positionStatus == null ? cristinPositionCodes : cristinPositionCodes.stream()
            .filter(position -> positionStatus.equals(position.isEnabled()))
            .collect(Collectors.toList());
    }

    private Set<PositionCode> mapPositionCodesToNva(List<CristinPositionCode> cristinPositionCodes) {
        return cristinPositionCodes.stream().map(CristinPositionCode::toPositionCode).collect(Collectors.toSet());
    }
}
