package no.unit.nva.cristin.funding.sources.client;

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

    private final transient URI cristinBaseUri;

    @JacocoGenerated
    public static CristinFundingSourcesApiClient defaultClient() {
        var httpClient = HttpClient.newBuilder().build();
        var cristinBaseUri = URI.create(new Environment().readEnv(EnvironmentKeys.ENV_KEY_CRISTIN_API_URL));
        return new CristinFundingSourcesApiClient(httpClient, cristinBaseUri);
    }

    public CristinFundingSourcesApiClient(HttpClient client, URI cristinBaseUri) {
        super(client);
        this.cristinBaseUri = cristinBaseUri;
    }

    public List<CristinFundingSource> queryFundingSources() throws ApiGatewayException {
        URI uri = getFundingSourcesUri();

        var response = fetchQueryResults(uri);

        checkHttpStatusCode(uri, response.statusCode(), response.body());

        try {
            return List.of(fromJson(response.body(), CristinFundingSource[].class));
        } catch (IOException e) {
            LOGGER.error("Unable to deserialize JSON", e);
            throw new BadGatewayException("Malformed response from Cristin!");
        }
    }

    public CristinFundingSource fetchFundingSource(String code) throws ApiGatewayException {
        var fundingSources = queryFundingSources();

        return fundingSources.stream()
            .filter(fundingSource -> fundingSource.getCode().equals(code))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Funding source not found: " + code));
    }

    private URI getFundingSourcesUri() {
        return UriWrapper.fromUri(this.cristinBaseUri).addChild("fundings", "sources").getUri();
    }
}
