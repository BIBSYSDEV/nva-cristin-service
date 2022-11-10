package no.unit.nva.cristin.projects;

import static java.util.Arrays.asList;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.attempt.Try;

public class CristinApiClient extends ApiClient {

    /**
     * Create a generic cristin API client with default HTTP client.
     */
    public CristinApiClient() {
        this(HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build());
    }


    public CristinApiClient(HttpClient client) {
        super(client);
    }

    protected List<CristinProject> getEnrichedProjectsUsingQueryResponse(HttpResponse<String> response, String language)
            throws ApiGatewayException {

        List<CristinProject> projectsFromQuery = asList(getDeserializedResponse(response, CristinProject[].class));
        List<URI> cristinUris = extractCristinUrisFromProjects(language, projectsFromQuery);
        List<HttpResponse<String>> individualResponses = fetchQueryResultsOneByOne(cristinUris);

        List<CristinProject> enrichedCristinProjects = mapValidResponsesToCristinProjects(individualResponses);

        return allProjectsWereEnriched(projectsFromQuery, enrichedCristinProjects)
                ? enrichedCristinProjects
                : combineResultsWithQueryInCaseEnrichmentFails(projectsFromQuery, enrichedCristinProjects);
    }

    protected List<CristinProject> combineResultsWithQueryInCaseEnrichmentFails(List<CristinProject> projectsFromQuery,
                                                                                List<CristinProject> enrichedProjects) {
        Set<String> enrichedProjectIds = enrichedProjects.stream()
                .map(CristinProject::getCristinProjectId)
                .collect(Collectors.toSet());

        List<CristinProject> missingProjects = projectsFromQuery.stream()
                .filter(queryProject -> !enrichedProjectIds.contains(queryProject.getCristinProjectId()))
                .collect(Collectors.toList());

        ArrayList<CristinProject> result = new ArrayList<>();
        result.addAll(enrichedProjects);
        result.addAll(missingProjects);
        return result;
    }

    private boolean allProjectsWereEnriched(List<CristinProject> projectsFromQuery,
                                            List<CristinProject> enrichedCristinProjects) {
        return projectsFromQuery.size() == enrichedCristinProjects.size();
    }

    private List<URI> extractCristinUrisFromProjects(String language, List<CristinProject> projectsFromQuery) {
        return projectsFromQuery.stream()
                .map(attempt(project -> generateGetProjectUri(project.getCristinProjectId(), language)))
                .map(Try::orElseThrow)
                .collect(Collectors.toList());
    }

    public URI generateGetProjectUri(String id, String language) {
        return CristinQuery.fromIdAndLanguage(id, language);
    }

    private List<CristinProject> mapValidResponsesToCristinProjects(List<HttpResponse<String>> responses) {
        return responses.stream()
                .map(attempt(response -> getDeserializedResponse(response, CristinProject.class)))
                .map(Try::orElseThrow)
                .filter(CristinProject::hasValidContent)
                .collect(Collectors.toList());
    }

    protected List<NvaProject> mapValidCristinProjectsToNvaProjects(List<CristinProject> cristinProjects) {
        return cristinProjects.stream()
                .filter(CristinProject::hasValidContent)
                .map(CristinProject::toNvaProject)
                .collect(Collectors.toList());
    }

}
