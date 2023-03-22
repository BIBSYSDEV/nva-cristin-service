package no.unit.nva.cristin.projects.query;

import static no.unit.nva.cristin.model.Constants.PROJECT_SEARCH_CONTEXT_URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.common.CristinProjectApiClient;
import no.unit.nva.cristin.projects.common.CristinQuery;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public class QueryCristinProjectApiClient extends CristinProjectApiClient {

    public QueryCristinProjectApiClient() {
        super();
    }

    public QueryCristinProjectApiClient(HttpClient client) {
        super(client);
    }

    /**
     * Creates a wrapper object containing Cristin Projects transformed to NvaProjects with additional metadata. Is used
     * for serialization to the client.
     *
     * @param cristinQuery CristinQuery from client containing title and language
     * @return a SearchResponse filled with transformed Cristin Projects and metadata
     * @throws ApiGatewayException if some errors happen we should return this to client
     */
    public SearchResponse<NvaProject> queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(
        CristinQuery cristinQuery) throws ApiGatewayException {

        final var startRequestTime = System.currentTimeMillis();
        final var response = queryProjects(cristinQuery);
        final var cristinProjects = getEnrichedProjectsUsingQueryResponse(response);
        final var nvaProjects = mapValidCristinProjectsToNvaProjects(cristinProjects);
        final var processingTime = calculateProcessingTime(startRequestTime, System.currentTimeMillis());

        final var id = cristinQuery.toNvaURI();

        return new SearchResponse<NvaProject>(id)
                   .withContext(PROJECT_SEARCH_CONTEXT_URL)
                   .withHits(nvaProjects)
                   .usingHeadersAndQueryParams(response.headers(), cristinQuery.toNvaParameters())
                   .withProcessingTime(processingTime);
    }

    protected HttpResponse<String> queryProjects(CristinQuery cristinQuery)
        throws ApiGatewayException {

        HttpResponse<String> response = fetchQueryResults(cristinQuery.toURI());
        checkHttpStatusCode(cristinQuery.toNvaURI(), response.statusCode(),response.body());

        return response;
    }
}
