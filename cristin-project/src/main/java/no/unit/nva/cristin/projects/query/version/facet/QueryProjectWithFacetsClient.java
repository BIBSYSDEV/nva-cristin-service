package no.unit.nva.cristin.projects.query.version.facet;

import static no.unit.nva.cristin.model.Constants.PROJECT_SEARCH_CONTEXT_URL;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import no.unit.nva.client.ClientVersion;
import no.unit.nva.cristin.common.client.CristinQueryApiClient;
import no.unit.nva.cristin.facet.CristinFacetConverter;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.common.CristinProjectApiClient;
import no.unit.nva.cristin.projects.common.QueryProject;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.cristin.query.CristinProjectSearchResponse;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryProjectWithFacetsClient extends CristinProjectApiClient
    implements ClientVersion, CristinQueryApiClient<QueryProject, NvaProject> {

    private static final Logger logger = LoggerFactory.getLogger(QueryProjectWithFacetsClient.class);

    public static final String VERSION_WITH_AGGREGATIONS = "2023-11-03-aggregations";
    public static final String CALLING_UPSTREAM_URI = "Calling upstream uri: ";

    @Override
    public String getClientVersion() {
        return VERSION_WITH_AGGREGATIONS;
    }

    @Override
    public SearchResponse<NvaProject> executeQuery(QueryProject queryProject) throws ApiGatewayException {
        var startRequestTime = System.currentTimeMillis();
        var response = queryProjects(queryProject);
        var mappedResponse = deserializeResponse(response);
        var projectsData = getProjectsData(mappedResponse);
        var cristinProjects = enrichProjects(projectsData);
        var projects = mapValidCristinProjectsToNvaProjects(cristinProjects);
        var endRequestTime = System.currentTimeMillis();
        var id = queryProject.toNvaFacetURI();
        var convertedFacets = new CristinFacetConverter(id)
                                  .convert(mappedResponse.facets())
                                  .getConverted();

        return new SearchResponse<NvaProject>(id)
                   .withContext(PROJECT_SEARCH_CONTEXT_URL)
                   .usingHeadersAndQueryParams(response.headers(), queryProject.toNvaParametersWithAddedFacets())
                   .withProcessingTime(calculateProcessingTime(startRequestTime, endRequestTime))
                   .withHits(projects)
                   .withAggregations(convertedFacets);
    }

    protected HttpResponse<String> queryProjects(QueryProject queryProject) throws ApiGatewayException {
        var cristinUri = queryProject.toCristinFacetURI();
        logger.info(CALLING_UPSTREAM_URI + cristinUri);
        var response = fetchQueryResults(cristinUri);
        var id = queryProject.toNvaFacetURI();
        checkHttpStatusCode(id, response.statusCode(), response.body());
        return response;
    }

    private CristinProjectSearchResponse deserializeResponse(HttpResponse<String> response) throws BadGatewayException {
        return getDeserializedResponse(response, CristinProjectSearchResponse.class);
    }

    private static List<CristinProject> getProjectsData(CristinProjectSearchResponse mappedResponse) {
        return Optional.ofNullable(mappedResponse.data())
                   .map(Arrays::asList)
                   .orElse(Collections.emptyList());
    }

    /**
     * Enrich list of CristinProject from query with more data by doing one extra lookup request per project.
     */
    private List<CristinProject> enrichProjects(List<CristinProject> cristinProjects) {
        var cristinUris = extractCristinUrisFromProjects(cristinProjects);
        var individualResponses = fetchQueryResultsOneByOne(cristinUris);
        var enrichedCristinProjects = mapValidResponsesToCristinProjects(individualResponses);

        return allProjectsWereEnriched(cristinProjects, enrichedCristinProjects)
                   ? enrichedCristinProjects
                   : combineResultsWithQueryInCaseEnrichmentFails(cristinProjects, enrichedCristinProjects);
    }

}
