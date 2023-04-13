package no.unit.nva.cristin.projects.query;

import static no.unit.nva.cristin.model.Constants.DEFAULT_NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.Constants.FIRST_PAGE;
import static no.unit.nva.cristin.projects.query.QueryCristinProjectHandlerTest.GRANT_ID_EXAMPLE;
import static no.unit.nva.cristin.projects.query.QueryCristinProjectHandlerTest.RANDOM_TITLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URI;
import java.util.Map;
import no.unit.nva.cristin.projects.common.QueryProject;
import nva.commons.apigateway.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;

public class QueryCristinProjectApiClientTest {

    private static final String QUERY_CRISTIN_PROJECTS_EXAMPLE_URI =
        "https://api.cristin-test.uio.no/v2/projects?page=1&per_page=5&title=reindeer";
    private static final String CRISTIN_API_GRANT_ID_SEARCH_EXAMPLE_URI =
        "https://api.cristin-test.uio.no/v2/projects?page=1&per_page=5&project_code=1234567";

    @Test
    void getsCristinUriWithTitleParamWhenCallingUriBuilderWithTitleQueryRequested() throws Exception {
        final var cristinQuery =
            QueryProject.builder()
                .withQuery(RANDOM_TITLE)
                .withItemsFromPage(FIRST_PAGE)
                .withItemsPerPage(DEFAULT_NUMBER_OF_RESULTS)
                .validate()
                .build();
        final var expectedUri = new URI(QUERY_CRISTIN_PROJECTS_EXAMPLE_URI);
        final var actualUri1 = generateCristinQueryProjectsUrl(cristinQuery.toNvaParameters());
        final var actualUri2 = cristinQuery.toURI();
        assertEquals(expectedUri, actualUri1);
        assertEquals(expectedUri, actualUri2);
    }

    @Test
    void getsCristinUriWithNoQueryParamWhenCallingUriBuilderWithTitleQueryRequested() throws Exception {
        final var params =
            QueryProject.builder()
                .withTitle(RANDOM_TITLE)
                .withItemsFromPage(FIRST_PAGE)
                .withItemsPerPage(DEFAULT_NUMBER_OF_RESULTS)
                .validate()
                .build()
                .toParameters();
        final var expectedUri = new URI(QUERY_CRISTIN_PROJECTS_EXAMPLE_URI);
        final var actualUri = generateCristinQueryProjectsUrl(params);
        assertEquals(expectedUri, actualUri);
    }

    @Test
    void getsCristinUriWithProjectCodeParamWhenCallingUriBuilderWithGrantIdQueryRequested() throws Exception {
        final var sourceCristinQuery =
            QueryProject.builder()
                .withQuery(GRANT_ID_EXAMPLE)
                .withItemsFromPage(FIRST_PAGE)
                .withItemsPerPage(DEFAULT_NUMBER_OF_RESULTS)
                .validate()
                .build();

        final var expectedUri = new URI(CRISTIN_API_GRANT_ID_SEARCH_EXAMPLE_URI);
        final var actualUri = generateCristinQueryProjectsUrl(sourceCristinQuery.toParameters());
        assertEquals(expectedUri, actualUri);
    }

    protected URI generateCristinQueryProjectsUrl(Map<String, String> parameters) throws BadRequestException {
        return
            QueryProject.builder()
                .fromQueryParameters(parameters)
                .validate()
                .build()
                .toURI();
    }
}
