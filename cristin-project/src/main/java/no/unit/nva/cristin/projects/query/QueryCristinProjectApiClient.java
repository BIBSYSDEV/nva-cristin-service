package no.unit.nva.cristin.projects.query;

import static no.unit.nva.cristin.model.Constants.PROJECT_SEARCH_CONTEXT_URL;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Map;
import no.unit.nva.cristin.model.Constants.QueryParameterKey;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.common.CristinProjectApiClient;
import no.unit.nva.cristin.projects.common.CristinQuery;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public class QueryCristinProjectApiClient extends CristinProjectApiClient {


    /**
     * Creates a wrapper object containing Cristin Projects transformed to NvaProjects with additional metadata. Is used
     * for serialization to the client.
     *
     * @param requestQueryParameters Request parameters from client containing title and language
     * @return a SearchResponse filled with transformed Cristin Projects and metadata
     * @throws ApiGatewayException if some errors happen we should return this to client
     */
    public SearchResponse<NvaProject> queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(
        Map<String, String> requestQueryParameters) throws ApiGatewayException {

        final var startRequestTime = System.currentTimeMillis();
        final var cristinQuery =
            new CristinQuery.Builder()
                .fromQueryParameters(requestQueryParameters)
                .validate()
                .build();

        final var response = queryProjects(cristinQuery);
        var cristinProjects =
            getEnrichedProjectsUsingQueryResponse(response, cristinQuery.getValue(QueryParameterKey.LANGUAGE));

        if (cristinProjects.isEmpty()) { // && queryParameterKey == GRANT_ID) {
            cristinProjects = getEnrichedProjectsUsingQueryResponse(
                queryProjects(cristinQuery),
                cristinQuery.getValue(QueryParameterKey.LANGUAGE));
        }

        final var nvaProjects = mapValidCristinProjectsToNvaProjects(cristinProjects);
        final var endRequestTime = System.currentTimeMillis();
        final var id = cristinQuery.toNvaURI();

        return new SearchResponse<NvaProject>(id)
                   .withContext(PROJECT_SEARCH_CONTEXT_URL)
                   .withHits(nvaProjects)
                   .usingHeadersAndQueryParams(response.headers(), cristinQuery.toNvaParameters())
                   .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime));
    }

    protected HttpResponse<String> queryProjects(CristinQuery cristinQuery)
        throws ApiGatewayException {

        URI uri = cristinQuery.toURI();
        HttpResponse<String> response = fetchQueryResults(uri);
        var id = cristinQuery.toNvaURI();
        checkHttpStatusCode(id, response.statusCode());
        return response;
    }
}
