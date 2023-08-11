package no.unit.nva.cristin.projects.common;

import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static nva.commons.core.StringUtils.EMPTY_STRING;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CristinProjectApiClientTest {

    public static final String EXAMPLE_TITLE = "Example Title";
    private static final URI LOCALHOST_URI = URI.create("http://localhost/cristin");
    private final Set<String> ids = Set.of("123", "456", "789");
    private static final String LANGUAGE_NB = "nb";

    final CristinProjectApiClient cristinApiClient = new CristinProjectApiClient();

    @Test
    void returnsListOfResultsFromBothQueryAndEnrichmentIfAnyEnrichmentsFail() {
        var queryProjects = getSomeCristinProjects();
        var enrichedProjects = new ArrayList<>(queryProjects);
        // Remove one element to fake a failed enrichment
        enrichedProjects.remove(enrichedProjects.size() - 1);

        assertThat(enrichedProjects.size(), not(queryProjects.size()));
        assertThat(getCristinIdsFromProjects(enrichedProjects), not(containsInAnyOrder(ids.toArray(String[]::new))));

        List<CristinProject> combinedProjects =
            cristinApiClient.combineResultsWithQueryInCaseEnrichmentFails(queryProjects, enrichedProjects);

        assertThat(combinedProjects.size(), equalTo(queryProjects.size()));
        assertThat(getCristinIdsFromProjects(combinedProjects), containsInAnyOrder(ids.toArray(String[]::new)));
    }

    @Test
    void returnsCalculateProcessingTime() {
        long expected = 42;
        long startTime = 0;
        long endTime = startTime + expected;
        long actualTime = cristinApiClient.calculateProcessingTime(startTime, endTime);
        assertEquals(expected, actualTime);
    }

    @Test
    void returnsFetchGetResultHandlesException() throws IOException, InterruptedException {
        var mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenThrow(new RuntimeException(""));
        final var cristinApiClient = new CristinProjectApiClient(mockHttpClient);
        assertThrows(RuntimeException.class, () ->  cristinApiClient.fetchGetResult(LOCALHOST_URI));
    }

    @Test
    void returnsDummyFetchGetResultForCodeCoverage() throws IOException, InterruptedException, ApiGatewayException {
        var mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> httpResponse =
            new HttpResponseFaker(EMPTY_STRING, HttpURLConnection.HTTP_INTERNAL_ERROR);
        when(mockHttpClient.<String>send(any(), any())).thenReturn(httpResponse);
        final var cristinApiClient = new CristinProjectApiClient(mockHttpClient);
        var result = cristinApiClient.fetchGetResult(LOCALHOST_URI);
        assertNotNull(result);
    }


    @Test
    void returnsFetchQueryResultsHandlesException() throws IOException, InterruptedException {
        var mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenThrow(new RuntimeException(""));
        final var cristinApiClient = new CristinProjectApiClient(mockHttpClient);
        assertThrows(RuntimeException.class, () ->  cristinApiClient.fetchQueryResults(LOCALHOST_URI));
    }

    @Test
    void returnsDummyFetchQueryResultsForCodeCoverage() throws IOException, InterruptedException, ApiGatewayException {
        var mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> httpResponse =
            new HttpResponseFaker(EMPTY_STRING, HttpURLConnection.HTTP_INTERNAL_ERROR);
        when(mockHttpClient.<String>send(any(), any())).thenReturn(httpResponse);
        final CristinProjectApiClient cristinApiClient = new CristinProjectApiClient(mockHttpClient);
        var result = cristinApiClient.fetchQueryResults(LOCALHOST_URI);
        assertNotNull(result);
    }

    private Set<String> getCristinIdsFromProjects(List<CristinProject> projects) {
        return projects.stream().map(CristinProject::getCristinProjectId).collect(Collectors.toSet());
    }

    private List<CristinProject> getSomeCristinProjects() {
        return ids.stream().map(this::getValidCristinProjectFromId).collect(Collectors.toList());
    }

    private CristinProject getValidCristinProjectFromId(String id) {
        CristinProject cristinProject = new CristinProject();
        cristinProject.setCristinProjectId(id);
        cristinProject.setTitle(Map.of(LANGUAGE_NB, EXAMPLE_TITLE));
        return cristinProject;
    }
}
