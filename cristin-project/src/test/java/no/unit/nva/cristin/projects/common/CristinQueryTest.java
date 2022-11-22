package no.unit.nva.cristin.projects.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URI;
import org.junit.jupiter.api.Test;

public class CristinQueryTest {

    private static final String RANDOM_TITLE = "reindeer";
    private static final String LANGUAGE_NB = "nb";
    private static final String PER_PAGE = "10";
    private static final String FROM_PAGE = "2";
    private static final String ID = "1234";
    private static final String PARENT_UNIT_ID = "185.90.0.0";
    private static final String QUERY_CRISTIN_PROJECTS_EXAMPLE_URI =
        "https://api.cristin-test.uio.no/v2/projects?per_page=10&parent_unit_id=185.90.0.0&page=2&title=reindeer&lang=nb";
    private static final String GET_ONE_CRISTIN_PROJECT_EXAMPLE_URI =
        "https://api.cristin-test.uio.no/v2/projects/1234?lang=nb";

    private static final String SORT = "start_date";
    private static final String INSTITUTION_ID = "uib";
    private static final String PROJECT_MANAGER = "st";
    private static final String PARTICIPANT = "St";
    private static final String FUNDING_SOURCE = "NFR";
    private static final String UNIT = "184.12.60.0";
    private static final String LEVELS = "7";
    private static final String QUERY_EXAMPLE_WITH_MULTIPLE_PARAMETERS =
            "https://api.cristin-test.uio.no/v2/projects?sort=end_date&institution=uib"
                    + "&project_manager=st&participant=st&funding_source=NFR&unit=184.12.60.0&levels=7";



    private static final String KEYWORD= "nature";
    private static final String APPROVAL_REFERENCE_ID= "";
    private static final String USER = "askeladd:185";
    private static final String APPROVED_BY = "";
    private static final String FUNDING = "NFR:1234";
    private static final String BIOBANK = "St";
    @Test
    void buildReturnsUriWithCustomParameterValuesWhenCustomParameterValuesAreSupplied() {
        URI uri = new CristinQuery().withTitle(RANDOM_TITLE).withLanguage(LANGUAGE_NB).withItemsPerPage(PER_PAGE)
            .withFromPage(FROM_PAGE).withParentUnitId(PARENT_UNIT_ID).toURI();
        assertEquals(QUERY_CRISTIN_PROJECTS_EXAMPLE_URI, uri.toString());
    }

    @Test
    void buildReturnsUriWithIdAndLanguageWhenIdAndLanguageParametersAreSupplied() {
        URI uri = CristinQuery.fromIdAndLanguage(ID, LANGUAGE_NB);
        assertEquals(GET_ONE_CRISTIN_PROJECT_EXAMPLE_URI, uri.toString());
    }

    @Test
    void buildReturnsUriWithExtendedListOfParameters() {
        URI uri = CristinQuery.fromIdAndLanguage(ID, LANGUAGE_NB);
        assertEquals(GET_ONE_CRISTIN_PROJECT_EXAMPLE_URI, uri.toString());
    }
}
