package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
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

import static no.unit.nva.cristin.model.Constants.DEFAULT_NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.Constants.FIRST_PAGE;
import static no.unit.nva.cristin.model.Constants.QueryType.QUERY_USING_GRANT_ID;
import static no.unit.nva.cristin.model.Constants.QueryType.QUERY_USING_TITLE;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.cristin.projects.FetchCristinProjectsTest.GRANT_ID_EXAMPLE;
import static no.unit.nva.cristin.projects.FetchCristinProjectsTest.LANGUAGE_NB;
import static no.unit.nva.cristin.projects.FetchCristinProjectsTest.RANDOM_TITLE;
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

public class CristinApiClientTest {

    private static final String QUERY_CRISTIN_PROJECTS_EXAMPLE_URI =
        "https://api.cristin.no/v2/projects?per_page=5&page=1&lang=nb&title=reindeer";
    private static final String CRISTIN_API_GRANT_ID_SEARCH_EXAMPLE_URI =
        "https://api.cristin.no/v2/projects?per_page=5&project_code=1234567&page=1&lang=nb";
    public static final String EXAMPLE_TITLE = "Example Title";
    private static final URI LOCALHOST_URI = URI.create("http://localhost/cristin");
    private final Set<String> ids = Set.of("123", "456", "789");

    final CristinApiClient cristinApiClient = new CristinApiClient();

    @Test
    void getsCristinUriWithTitleParamWhenCallingUriBuilderWithTitleQueryRequested() throws Exception {
        Map<String, String> params = Map.of(
            QUERY, RANDOM_TITLE,
            LANGUAGE, LANGUAGE_NB,
            PAGE, FIRST_PAGE,
            NUMBER_OF_RESULTS, DEFAULT_NUMBER_OF_RESULTS);
        URI expectedUri = new URI(QUERY_CRISTIN_PROJECTS_EXAMPLE_URI);

        assertEquals(expectedUri, cristinApiClient.generateQueryProjectsUrl(params, QUERY_USING_TITLE));
    }

    @Test
    void getsCristinUriWithProjectCodeParamWhenCallingUriBuilderWithGrantIdQueryRequested() throws Exception {
        Map<String, String> params = Map.of(
            QUERY, GRANT_ID_EXAMPLE,
            LANGUAGE, LANGUAGE_NB,
            PAGE, FIRST_PAGE,
            NUMBER_OF_RESULTS, DEFAULT_NUMBER_OF_RESULTS);
        URI expectedUri = new URI(CRISTIN_API_GRANT_ID_SEARCH_EXAMPLE_URI);

        assertEquals(expectedUri, cristinApiClient.generateQueryProjectsUrl(params, QUERY_USING_GRANT_ID));
    }

    @Test
    void returnsListOfResultsFromBothQueryAndEnrichmentIfAnyEnrichmentsFail() {
        List<CristinProject> queryProjects = getSomeCristinProjects();
        List<CristinProject> enrichedProjects = new ArrayList<>(queryProjects);
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
        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenThrow(new RuntimeException(""));
        final CristinApiClient cristinApiClient = new CristinApiClient(mockHttpClient);
        assertThrows(RuntimeException.class, () ->  cristinApiClient.fetchGetResult(LOCALHOST_URI));
    }

    @Test
    void returnsDummyFetchGetResultForCodeCoverage() throws IOException, InterruptedException {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> httpResponse =
            new HttpResponseFaker(EMPTY_STRING, HttpURLConnection.HTTP_INTERNAL_ERROR);
        when(mockHttpClient.<String>send(any(), any())).thenReturn(httpResponse);
        final CristinApiClient cristinApiClient = new CristinApiClient(mockHttpClient);
        HttpResponse<String> result = cristinApiClient.fetchGetResult(LOCALHOST_URI);
        assertNotNull(result);
    }


    @Test
    void returnsFetchQueryResultsHandlesException() throws IOException, InterruptedException {
        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(), any())).thenThrow(new RuntimeException(""));
        final CristinApiClient cristinApiClient = new CristinApiClient(mockHttpClient);
        assertThrows(RuntimeException.class, () ->  cristinApiClient.fetchQueryResults(LOCALHOST_URI));
    }

    @Test
    void returnsDummyFetchQueryResultsForCodeCoverage() throws IOException, InterruptedException {
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse<String> httpResponse =
            new HttpResponseFaker(EMPTY_STRING, HttpURLConnection.HTTP_INTERNAL_ERROR);
        when(mockHttpClient.<String>send(any(), any())).thenReturn(httpResponse);
        final CristinApiClient cristinApiClient = new CristinApiClient(mockHttpClient);
        HttpResponse<String> result = cristinApiClient.fetchQueryResults(LOCALHOST_URI);
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
