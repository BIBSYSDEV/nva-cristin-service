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

    private static final String SAMPLE_SORT = "start_date";
    private static final String SAMPLE_INSTITUTION_ID = "uib";
    private static final String SAMPLE_PROJECT_MANAGER = "st";
    private static final String SAMPLE_PARTICIPANT = "St";
    private static final String SAMPLE_FUNDING_SOURCE = "NFR";
    private static final String SAMPLE_UNIT = "184.12.60.0";
    private static final String SAMPLE_LEVELS = "7";
    private static final String QUERY_SAMPLE_WITH_MULTIPLE_PARAMETERS =
            "https://api.cristin-test.uio.no/v2/projects?per_page=5&institution=uib&unit=184.12.60.0"
                    + "&funding_source=NFR&page=1&sort=start_date&project_manager=st&participant=St&levels=7";

    private static final String KEYWORD= "nature";
    private static final String BIOBANK = "533895";
    private static final String KEYWORD_QUERY =
            "https://api.cristin-test.uio.no/v2/projects?per_page=5&page=1&keyword=nature&biobank=533895";

    private static final String APPROVAL_REFERENCE_ID= "2017/1593";
    private static final String APPROVED_BY = "REK";
    private static final String QUERY_APPROVED_PARAM =
        "https://api.cristin-test.uio.no/v2/projects?per_page=5&approved_by=REK&page=1&approval_reference_id=2017/1593";


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
        URI uri = new CristinQuery().withSort(SAMPLE_SORT).withInstitution(SAMPLE_INSTITUTION_ID)
                .withProjectManager(SAMPLE_PROJECT_MANAGER)
                .withParticipant(SAMPLE_PARTICIPANT)
                .withFundingSource(SAMPLE_FUNDING_SOURCE)
                .withUnit(SAMPLE_UNIT)
                .withLevels(SAMPLE_LEVELS)
                .toURI();
        assertEquals(QUERY_SAMPLE_WITH_MULTIPLE_PARAMETERS, uri.toString());
    }

    @Test
    void buildReturnsUriWithKeyword() {
        URI uri = new CristinQuery().withKeyword(KEYWORD).withBiobank(BIOBANK).toURI();
        assertEquals(KEYWORD_QUERY, uri.toString());
    }

    @Test
    void buildReturnsUriApproved() {
        URI uri = new CristinQuery().withApprovedBy(APPROVED_BY).withApprovalReferenceId(APPROVAL_REFERENCE_ID).toURI();
        assertEquals(QUERY_APPROVED_PARAM, uri.toString());
    }


}
