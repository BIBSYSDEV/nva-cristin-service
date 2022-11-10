package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_QUERY_WITH_PARAMS_FAILED;
import static no.unit.nva.cristin.model.Constants.PROJECT_SEARCH_CONTEXT_URL;
import static no.unit.nva.cristin.model.Constants.QueryType.QUERY_USING_GRANT_ID;
import static no.unit.nva.cristin.model.Constants.QueryType.QUERY_USING_TITLE;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.utils.UriUtils.PROJECT;
import static no.unit.nva.utils.UriUtils.createIdUriFromParams;
import static no.unit.nva.utils.UriUtils.queryParameters;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.model.Constants.QueryType;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;

public class QueryCristinProjectApiClient extends CristinApiClient {

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

        long startRequestTime = System.currentTimeMillis();
        QueryType queryType = getQueryTypeBasedOnParameters(requestQueryParameters);
        HttpResponse<String> response = queryProjects(requestQueryParameters, queryType);
        List<CristinProject> cristinProjects =
            getEnrichedProjectsUsingQueryResponse(response, requestQueryParameters.get(LANGUAGE));
        if (cristinProjects.isEmpty() && queryType == QUERY_USING_GRANT_ID) {
            response = queryProjects(requestQueryParameters, QUERY_USING_TITLE);
            cristinProjects = getEnrichedProjectsUsingQueryResponse(response, requestQueryParameters.get(LANGUAGE));
        }
        List<NvaProject> nvaProjects = mapValidCristinProjectsToNvaProjects(cristinProjects);
        long endRequestTime = System.currentTimeMillis();

        URI id = createIdUriFromParams(rewrapOrganizationUri(requestQueryParameters), PROJECT);

        return new SearchResponse<NvaProject>(id)
                   .withContext(PROJECT_SEARCH_CONTEXT_URL)
                   .usingHeadersAndQueryParams(response.headers(), requestQueryParameters)
                   .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime))
                   .withHits(nvaProjects);
    }

    protected HttpResponse<String> queryProjects(Map<String, String> parameters, QueryType queryType)
        throws ApiGatewayException {

        URI uri = attempt(() -> generateQueryProjectsUrl(parameters, queryType))
                      .toOptional(failure -> logError(ERROR_MESSAGE_QUERY_WITH_PARAMS_FAILED, queryParameters(parameters),
                                                      failure.getException()))
                      .orElseThrow();
        HttpResponse<String> response = fetchQueryResults(uri);
        URI id = createIdUriFromParams(parameters, PROJECT);
        checkHttpStatusCode(id, response.statusCode());
        return response;
    }

    protected URI generateQueryProjectsUrl(Map<String, String> parameters, QueryType queryType) {
        CristinQuery query = new CristinQuery().generateQueryParameters(parameters);

        return queryType == QUERY_USING_GRANT_ID
                   ? query.withGrantId(parameters.get(QUERY)).toURI() : query.withTitle(parameters.get(QUERY)).toURI();
    }

    private QueryType getQueryTypeBasedOnParameters(Map<String, String> requestQueryParams) {
        return Utils.isPositiveInteger(requestQueryParams.get(QUERY)) ? QUERY_USING_GRANT_ID : QUERY_USING_TITLE;
    }

    private Map<String, String> rewrapOrganizationUri(Map<String, String> requestQueryParameters) {
        if (requestQueryParameters.containsKey(ORGANIZATION)) {
            String organizationId = requestQueryParameters.get(ORGANIZATION);
            requestQueryParameters.put(ORGANIZATION, URLEncoder.encode(organizationId, StandardCharsets.UTF_8));
        }
        return requestQueryParameters;
    }

}
