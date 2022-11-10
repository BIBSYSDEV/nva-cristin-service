package no.unit.nva.cristin.projects.query;

import static no.unit.nva.cristin.model.Constants.DEFAULT_NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.Constants.FIRST_PAGE;
import static no.unit.nva.cristin.model.Constants.QueryType.QUERY_USING_GRANT_ID;
import static no.unit.nva.cristin.model.Constants.QueryType.QUERY_USING_TITLE;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static no.unit.nva.cristin.projects.query.QueryCristinProjectHandlerTest.GRANT_ID_EXAMPLE;
import static no.unit.nva.cristin.projects.query.QueryCristinProjectHandlerTest.LANGUAGE_NB;
import static no.unit.nva.cristin.projects.query.QueryCristinProjectHandlerTest.RANDOM_TITLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class QueryCristinProjectApiClientTest {

    private static final String QUERY_CRISTIN_PROJECTS_EXAMPLE_URI =
        "https://api.cristin-test.uio.no/v2/projects?per_page=5&page=1&lang=nb&title=reindeer";
    private static final String CRISTIN_API_GRANT_ID_SEARCH_EXAMPLE_URI =
        "https://api.cristin-test.uio.no/v2/projects?per_page=5&project_code=1234567&page=1&lang=nb";

    final QueryCristinProjectApiClient cristinApiClient = new QueryCristinProjectApiClient();

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
