package no.unit.nva.cristin.funding.sources.client;

import static no.unit.nva.client.HttpClientProvider.defaultHttpClient;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.funding.sources.common.EnvironmentKeys;
import no.unit.nva.cristin.funding.sources.model.cristin.CristinFundingSource;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CristinFundingSourcesApiClient extends ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CristinFundingSourcesApiClient.class);

    public static final String MALFORMED_RESPONSE_FROM_CRISTIN = "Malformed response from Cristin!";
    public static final String UNABLE_TO_DESERIALIZE_JSON = "Unable to deserialize JSON";
    public static final String FUNDING_SOURCE_NOT_FOUND = "Funding source not found";
    public static final String FUNDINGS_PATH = "fundings";
    public static final String SOURCES_PATH = "sources";

    private final transient URI cristinBaseUri;

    @JacocoGenerated
    public static CristinFundingSourcesApiClient defaultClient() {
        var httpClient = defaultHttpClient();
        var cristinBaseUri = getCristinBaseUri();

        return new CristinFundingSourcesApiClient(httpClient, cristinBaseUri);
    }

    private static URI getCristinBaseUri() {
        return URI.create(new Environment().readEnv(EnvironmentKeys.ENV_KEY_CRISTIN_API_URL));
    }

    public CristinFundingSourcesApiClient(HttpClient client, URI cristinBaseUri) {
        super(client);
        this.cristinBaseUri = cristinBaseUri;
    }

    public List<CristinFundingSource> queryFundingSources() throws ApiGatewayException {
        var uri = getFundingSourcesUri();
        var response = fetchQueryResults(uri);

        checkHttpStatusCode(uri, response.statusCode(), response.body());

        try {
            return List.of(fromJson(response.body(), CristinFundingSource[].class));
        } catch (IOException e) {
            LOGGER.error(UNABLE_TO_DESERIALIZE_JSON, e);
            throw new BadGatewayException(MALFORMED_RESPONSE_FROM_CRISTIN);
        }
    }

    public CristinFundingSource fetchFundingSource(String code) throws ApiGatewayException {
        var fundingSources = queryFundingSources();

        return fundingSources.stream()
            .filter(fundingSource -> fundingSource.code().equals(code))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(FUNDING_SOURCE_NOT_FOUND));
    }

    private URI getFundingSourcesUri() {
        return UriWrapper.fromUri(this.cristinBaseUri).addChild(FUNDINGS_PATH, SOURCES_PATH).getUri();
    }
}
