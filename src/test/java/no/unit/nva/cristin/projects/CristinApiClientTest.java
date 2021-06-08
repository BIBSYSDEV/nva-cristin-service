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
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class CristinApiClientTest {

    private static final String QUERY_CRISTIN_PROJECTS_EXAMPLE_URI =
        "https://api.cristin.no/v2/projects/?lang=nb&page=1&per_page=5&title=reindeer";
    private static final String CRISTIN_API_GRANT_ID_SEARCH_EXAMPLE_URI =
        "https://api.cristin.no/v2/projects/?lang=nb&page=1&per_page=5&project_code=1234567";

    CristinApiClient cristinApiClient = new CristinApiClient();

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
}
