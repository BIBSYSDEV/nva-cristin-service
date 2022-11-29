package no.unit.nva.cristin.projects.query.organization;

import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PROJECTS_PATH;
import static no.unit.nva.cristin.model.Constants.PROJECT_SEARCH_CONTEXT_URL;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.projects.common.CristinQuery.CRISTIN_QUERY_PARAMETER_PARENT_UNIT_ID;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.common.CristinProjectApiClient;
import no.unit.nva.cristin.projects.common.CristinQuery;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.paths.UriWrapper;

public class QueryCristinOrganizationProjectApiClient extends CristinProjectApiClient {

    /**
     * Searches for an Organizations projects for a given parent_unit.
     * @param requestQueryParameters parametes for search containg parent_unit_id
     * @return a SearchResponse filled with transformed Cristin Projects and metadata
     * @throws ApiGatewayException if some errors happen we should return this to client
     */
    public SearchResponse<NvaProject> listOrganizationProjects(Map<String, String> requestQueryParameters)
        throws ApiGatewayException {

        long startRequestTime = System.currentTimeMillis();
        URI cristinUri = new CristinQuery()
                .generateQueryParameters(requestQueryParameters)
                .withParentUnitId(requestQueryParameters.get(CRISTIN_QUERY_PARAMETER_PARENT_UNIT_ID))
                .toURI();
        HttpResponse<String> response = listProjects(cristinUri);
        List<CristinProject> cristinProjects =
            getEnrichedProjectsUsingQueryResponse(response, requestQueryParameters.get(LANGUAGE));
        List<NvaProject> nvaProjects = mapValidCristinProjectsToNvaProjects(cristinProjects);
        long endRequestTime = System.currentTimeMillis();

        URI id = getServiceUri(new HashMap<>(requestQueryParameters));

        return new SearchResponse<NvaProject>(id)
                   .withContext(PROJECT_SEARCH_CONTEXT_URL)
                   .usingHeadersAndQueryParams(response.headers(), requestQueryParameters)
                   .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime))
                   .withHits(nvaProjects);
    }

    protected HttpResponse<String> listProjects(URI uri) throws ApiGatewayException {
        HttpResponse<String> response = fetchQueryResults(uri);
        checkHttpStatusCode(uri, response.statusCode());
        return response;
    }

    private URI getServiceUri(Map<String, String> queryParameters) {
        final String identifier = queryParameters.remove(CRISTIN_QUERY_PARAMETER_PARENT_UNIT_ID);
        return new UriWrapper(HTTPS,
                              DOMAIN_NAME).addChild(BASE_PATH)
                   .addChild(ORGANIZATION_PATH)
                   .addChild(identifier)
                   .addChild(PROJECTS_PATH)
                   .addQueryParameters(queryParameters)
                   .getUri();
    }

}
