package no.unit.nva.cristin.projects.common;

import static java.util.Arrays.asList;
import static no.unit.nva.HttpClientProvider.defaultHttpClient;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.model.CristinQuery;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.attempt.Try;

@SuppressWarnings({"PMD.DataflowAnomalyAnalysis"})
public class CristinProjectApiClient extends ApiClient {

    /**
     * Create a generic cristin API client with default HTTP client.
     */
    public CristinProjectApiClient() {
        this(defaultHttpClient());
    }


    public CristinProjectApiClient(HttpClient client) {
        super(client);
    }

    /**
     * Takes a query response from upstream, extracts all project URI's, and fetches them one by one before merging
     * them into a list of CristinProject. If individual fetch fails, it uses the query response for that project.
     */
    public List<CristinProject> getEnrichedProjectsUsingQueryResponse(HttpResponse<String> response)
            throws ApiGatewayException {

        List<CristinProject> projectsFromQuery = asList(getDeserializedResponse(response, CristinProject[].class));
        List<URI> cristinUris = extractCristinUrisFromProjects(projectsFromQuery);
        List<HttpResponse<String>> individualResponses = fetchQueryResultsOneByOne(cristinUris);

        List<CristinProject> enrichedCristinProjects = mapValidResponsesToCristinProjects(individualResponses);

        return allProjectsWereEnriched(projectsFromQuery, enrichedCristinProjects)
                ? enrichedCristinProjects
                : combineResultsWithQueryInCaseEnrichmentFails(projectsFromQuery, enrichedCristinProjects);
    }

    protected List<CristinProject> combineResultsWithQueryInCaseEnrichmentFails(List<CristinProject> projectsFromQuery,
                                                                                List<CristinProject> enrichedProjects) {
        final var enrichedProjectIds = getEnrichedProjectIds(enrichedProjects);

        var missingProjects = projectsFromQuery.stream()
                .filter(queryProject -> !enrichedProjectIds.contains(queryProject.getCristinProjectId()));

        return Stream.concat(enrichedProjects.stream(), missingProjects).toList();
    }

    private Set<String> getEnrichedProjectIds(List<CristinProject> enrichedProjects) {
        return enrichedProjects.stream()
                   .map(CristinProject::getCristinProjectId)
                   .collect(Collectors.toSet());
    }

    private boolean allProjectsWereEnriched(List<CristinProject> projectsFromQuery,
                                            List<CristinProject> enrichedCristinProjects) {
        return projectsFromQuery.size() == enrichedCristinProjects.size();
    }

    private List<URI> extractCristinUrisFromProjects(List<CristinProject> projectsFromQuery) {
        return projectsFromQuery.stream()
                   .map(CristinProject::getCristinProjectId)
                   .map(CristinQuery::fromIdentifier)
                   .toList();
    }

    private List<CristinProject> mapValidResponsesToCristinProjects(List<HttpResponse<String>> responses) {
        return responses.stream()
                .map(attempt(response -> getDeserializedResponse(response, CristinProject.class)))
                .map(Try::orElseThrow)
                .filter(CristinProject::hasValidContent)
                .toList();
    }

    protected List<NvaProject> mapValidCristinProjectsToNvaProjects(List<CristinProject> cristinProjects) {
        return cristinProjects.stream()
                .filter(CristinProject::hasValidContent)
                .map(CristinProject::toNvaProject)
                .toList();
    }

}
