package no.unit.nva.cristin.projects.query;

import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.projects.common.CristinProjectApiClient;
import no.unit.nva.cristin.projects.common.QueryProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;

import static no.unit.nva.cristin.model.Constants.PROJECT_SEARCH_CONTEXT_URL;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.LANGUAGE;

public class QueryCristinProjectApiClient extends CristinProjectApiClient {

    private static final Logger logger = LoggerFactory.getLogger(QueryCristinProjectApiClient.class);

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
        final var cristinProjects = getEnrichedProjectsUsingQueryResponse(response, queryProject.getValue(LANGUAGE));
        final var nvaProjects = mapValidCristinProjectsToNvaProjects(cristinProjects);
        final var processingTime = calculateProcessingTime(startRequestTime, System.currentTimeMillis());

        final var id = queryProject.toNvaURI();

        return new SearchResponse<NvaProject>(id)
                   .withContext(PROJECT_SEARCH_CONTEXT_URL)
                   .withHits(nvaProjects)
                   .usingHeadersAndQueryParams(response.headers(), queryProject.toNvaParameters())
                   .withProcessingTime(processingTime);
    }

    protected HttpResponse<String> queryProjects(QueryProject queryProject)
        throws ApiGatewayException {
        logger.info(queryProject.toURI().toString());
        logger.info(queryProject.toNvaURI().toString());

        HttpResponse<String> response = fetchQueryResults(queryProject.toURI());
        checkHttpStatusCode(queryProject.toNvaURI(), response.statusCode(), response.body());

        return response;
    }
}
