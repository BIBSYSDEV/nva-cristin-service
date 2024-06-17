package no.unit.nva.biobank.client;

import static no.unit.nva.client.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.cristin.model.Constants.PROJECT_SEARCH_CONTEXT_URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
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
    public static CristinBiobankApiClient defaultClient() {
        return new CristinBiobankApiClient(defaultHttpClient());
    }

    public CristinBiobankApiClient(HttpClient client) {
        super(client);
    }

    public Biobank fetchBiobank(QueryBiobank query) throws ApiGatewayException {
        var response = httpRequestWithStatusCheck(query);
        var cristinBiobank = getDeserializedResponse(response, CristinBiobank.class);
        return cristinBiobank.toBiobank();
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
    public SearchResponse<Biobank> fetchBiobanksWithParameters(QueryBiobank query) throws ApiGatewayException {
        final var startRequestTime = System.currentTimeMillis();
        final var response = httpRequestWithStatusCheck(query);
        final var cristinBiobanks = getDeserializedResponse(response, CristinBiobank[].class);
        final var nvaBiobanks = toNvaFormat(cristinBiobanks);
        final var processingTime = calculateProcessingTime(startRequestTime, System.currentTimeMillis());
        final var id = query.toNvaURI();

        return new SearchResponse<Biobank>(id)
                   .withContext(PROJECT_SEARCH_CONTEXT_URL)
                   .withHits(nvaBiobanks)
                   .usingHeadersAndQueryParams(response.headers(), query.toNvaParameters())
                   .withProcessingTime(processingTime);
    }

    protected HttpResponse<String> httpRequestWithStatusCheck(QueryBiobank query) throws ApiGatewayException {
        HttpResponse<String> response = fetchQueryResults(query.toURI());
        checkHttpStatusCode(query.toNvaURI(), response.statusCode(), response.body());

        return response;
    }

    private List<Biobank> toNvaFormat(CristinBiobank[] cristinBiobanks) {
        return Arrays.stream(cristinBiobanks)
                   .map(CristinBiobank::toBiobank)
                   .collect(Collectors.toList());
    }

}
