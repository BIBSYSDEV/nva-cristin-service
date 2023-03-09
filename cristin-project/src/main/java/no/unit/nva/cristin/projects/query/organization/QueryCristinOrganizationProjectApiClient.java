package no.unit.nva.cristin.projects.query.organization;

import static no.unit.nva.cristin.model.Constants.PROJECT_SEARCH_CONTEXT_URL;
import static no.unit.nva.cristin.model.QueryParameterKey.LANGUAGE;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.common.CristinProjectApiClient;
import no.unit.nva.cristin.projects.common.CristinQuery;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public class QueryCristinOrganizationProjectApiClient extends CristinProjectApiClient {

    public QueryCristinOrganizationProjectApiClient() {
        super();
    }

    public QueryCristinOrganizationProjectApiClient(HttpClient httpClient) {
        super(httpClient);
    }

    /**
     * Searches for an Organizations projects for a given parent_unit.
     *
     * @param cristinQuery parametes for search containg parent_unit_id
     * @return a SearchResponse filled with transformed Cristin Projects and metadata
     * @throws ApiGatewayException if some errors happen we should return this to client
     */
    public SearchResponse<NvaProject> listOrganizationProjects(CristinQuery cristinQuery)
        throws ApiGatewayException {

        var startRequestTime = System.currentTimeMillis();
        var cristinUri = cristinQuery.toURI();

        var response = listProjects(cristinUri);
        var cristinProjects =
            getEnrichedProjectsUsingQueryResponse(response, cristinQuery.getValue(LANGUAGE));
        var nvaProjects = mapValidCristinProjectsToNvaProjects(cristinProjects);
        var endRequestTime = System.currentTimeMillis();

        URI id = cristinQuery.toNvaURI();

        return new SearchResponse<NvaProject>(id)
                   .withContext(PROJECT_SEARCH_CONTEXT_URL)
                   .usingHeadersAndQueryParams(response.headers(), cristinQuery.toNvaParameters())
                   .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime))
                   .withHits(nvaProjects);
    }

    protected HttpResponse<String> listProjects(URI uri) throws ApiGatewayException {
        HttpResponse<String> response = fetchQueryResults(uri);
        checkHttpStatusCode(uri, response.statusCode());
        return response;
    }

}
