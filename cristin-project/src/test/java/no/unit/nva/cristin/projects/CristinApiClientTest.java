package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.Constants.DEFAULT_NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.projects.Constants.FIRST_PAGE;
import static no.unit.nva.cristin.projects.Constants.LANGUAGE;
import static no.unit.nva.cristin.projects.Constants.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.projects.Constants.PAGE;
import static no.unit.nva.cristin.projects.Constants.QUERY;
import static no.unit.nva.cristin.projects.Constants.QueryType.QUERY_USING_GRANT_ID;
import static no.unit.nva.cristin.projects.Constants.QueryType.QUERY_USING_TITLE;
import static no.unit.nva.cristin.projects.FetchCristinProjectsTest.GRANT_ID_EXAMPLE;
import static no.unit.nva.cristin.projects.FetchCristinProjectsTest.LANGUAGE_NB;
import static no.unit.nva.cristin.projects.FetchCristinProjectsTest.RANDOM_TITLE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import org.junit.jupiter.api.Test;

public class CristinApiClientTest {

    private static final String QUERY_CRISTIN_PROJECTS_EXAMPLE_URI =
        "https://api.cristin.no/v2/projects/?lang=nb&page=1&per_page=5&title=reindeer";
    private static final String CRISTIN_API_GRANT_ID_SEARCH_EXAMPLE_URI =
        "https://api.cristin.no/v2/projects/?lang=nb&page=1&per_page=5&project_code=1234567";
    public static final String EXAMPLE_TITLE = "Example Title";
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
