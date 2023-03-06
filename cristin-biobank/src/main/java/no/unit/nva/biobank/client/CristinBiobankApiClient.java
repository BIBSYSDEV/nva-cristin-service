package no.unit.nva.biobank.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;
import no.unit.nva.biobank.common.EnvironmentKeys;
import no.unit.nva.biobank.model.cristin.CristinBiobank;
import no.unit.nva.cristin.common.client.ApiClient;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CristinBiobankApiClient extends ApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(CristinBiobankApiClient.class);

    private final transient URI cristinBaseUri;

    @JacocoGenerated
    public static CristinBiobankApiClient defaultClient() {
        var httpClient = HttpClient.newBuilder().build();
        var cristinBaseUri = URI.create(new Environment().readEnv(EnvironmentKeys.ENV_KEY_CRISTIN_API_URL));
        return new CristinBiobankApiClient(httpClient, cristinBaseUri);
    }

    public CristinBiobankApiClient(HttpClient client, URI cristinBaseUri) {
        super(client);
        this.cristinBaseUri = cristinBaseUri;
    }

    public List<CristinBiobank> queryBiobanks() throws ApiGatewayException {
        URI uri = getBiobanksUri();

        var response = fetchQueryResults(uri);

        checkHttpStatusCode(uri, response.statusCode());

        try {
            return List.of(fromJson(response.body(), CristinBiobank[].class));
        } catch (IOException e) {
            LOGGER.error("Unable to deserialize JSON", e);
            throw new BadGatewayException("Malformed response from Cristin!");
        }
    }

    public CristinBiobank fetchBiobank(String identifier) throws ApiGatewayException {
        var fundingSources = queryBiobanks();

        return fundingSources.stream()
            .filter(biobank -> biobank.getBiobankId().equals(identifier))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Biobank not found: " + identifier));
    }

    private URI getBiobanksUri() {
        return UriWrapper.fromUri(this.cristinBaseUri).addChild("biobank", "sources").getUri();
    }
}
