package no.unit.nva.biobank.client;

import static no.unit.nva.cristin.model.Constants.PROJECT_SEARCH_CONTEXT_URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;
import no.unit.nva.biobank.model.QueryBiobank;
import no.unit.nva.biobank.model.cristin.CristinBiobank;
import no.unit.nva.biobank.model.nva.Biobank;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.JacocoGenerated;

public class CristinBiobankApiClient extends ApiClient {

    @JacocoGenerated
    public static CristinBiobankApiClient defaultClient =
        new CristinBiobankApiClient(
            HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build()
        );

    public CristinBiobankApiClient(HttpClient client) {
        super(client);
    }

    public CristinBiobank fetchBiobank(QueryBiobank query) throws ApiGatewayException {
        var response = queryBiobank(query);
        return getDeserializedResponse(response, CristinBiobank.class);
    }

    /**
     * Creates a wrapper object containing CristinBiobank transformed to Biobank with additional metadata. Is used for
     * serialization to the client.
     *
     * @param query from client containing title and language
     * @return a search response filled with transformed Cristin Biobank and metadata
     * @throws ApiGatewayException if some errors happen we should return this to client
     */
    @JacocoGenerated
    public SearchResponse<Biobank> queryBiobankWithMetadata(QueryBiobank query) throws ApiGatewayException {

        final var startRequestTime = System.currentTimeMillis();
        final var response = queryBiobank(query);
        final var nvaBiobanks =
            Arrays.stream(getDeserializedResponse(response, CristinBiobank[].class))
                .map(CristinBiobank::toBiobank)
                .collect(Collectors.toList());

        final var processingTime = calculateProcessingTime(startRequestTime, System.currentTimeMillis());
        final var id = query.toNvaURI();

        return
            new SearchResponse<Biobank>(id)
                .withContext(PROJECT_SEARCH_CONTEXT_URL)
                .withHits(nvaBiobanks)
                .usingHeadersAndQueryParams(response.headers(), query.toNvaParameters())
                .withProcessingTime(processingTime);
    }

    protected HttpResponse<String> queryBiobank(QueryBiobank query) throws ApiGatewayException {

        HttpResponse<String> response = fetchQueryResults(query.toURI());
        checkHttpStatusCode(query.toNvaURI(), response.statusCode(), response.body());

        return response;
    }
}
