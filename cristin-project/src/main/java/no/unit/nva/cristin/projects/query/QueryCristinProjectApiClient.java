package no.unit.nva.cristin.projects.query;

import static no.unit.nva.client.ClientProvider.VERSION_ONE;
import static no.unit.nva.cristin.model.Constants.PROJECT_SEARCH_CONTEXT_URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import no.unit.nva.client.ClientVersion;
import no.unit.nva.cristin.common.client.CristinQueryApiClient;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.common.CristinProjectApiClient;
import no.unit.nva.cristin.projects.common.QueryProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public class QueryCristinProjectApiClient extends CristinProjectApiClient
    implements ClientVersion, CristinQueryApiClient<QueryProject, NvaProject> {

    public QueryCristinProjectApiClient() {
        super();
    }

    public QueryCristinProjectApiClient(HttpClient client) {
        super(client);
    }

    @Override
    public SearchResponse<NvaProject> executeQuery(QueryProject queryProject) throws ApiGatewayException {
        return queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(queryProject);
    }

    @Override
    public String getClientVersion() {
        return VERSION_ONE;
    }

    /**
     * Creates a wrapper object containing Cristin Projects transformed to NvaProjects with additional metadata. Is used
     * for serialization to the client.
     *
     * @param queryProject QueryProject from client containing title and language
     * @return a SearchResponse filled with transformed Cristin Projects and metadata
     * @throws ApiGatewayException if some errors happen we should return this to client
     */
    public SearchResponse<NvaProject> queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(
        QueryProject queryProject) throws ApiGatewayException {

        final var startRequestTime = System.currentTimeMillis();
        final var response = queryProjects(queryProject);
        final var cristinProjects = getEnrichedProjectsUsingQueryResponse(response);
        final var nvaProjects = mapValidCristinProjectsToNvaProjects(cristinProjects);
        final var processingTime = calculateProcessingTime(startRequestTime, System.currentTimeMillis());

        final var id = queryProject.toNvaURI();

        return new SearchResponse<NvaProject>(id)
                   .withContext(PROJECT_SEARCH_CONTEXT_URL)
                   .withHits(nvaProjects)
                   .usingHeadersAndQueryParams(response.headers(), queryProject.toNvaParameters())
                   .withProcessingTime(processingTime);
    }

    protected HttpResponse<String> queryProjects(QueryProject queryProject) throws ApiGatewayException {
        var response = fetchQueryResults(queryProject.toURI());
        checkHttpStatusCode(queryProject.toNvaURI(), response.statusCode(), response.body());

        return response;
    }
}
