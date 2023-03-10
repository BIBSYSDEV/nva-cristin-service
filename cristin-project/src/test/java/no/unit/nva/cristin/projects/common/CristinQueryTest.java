package no.unit.nva.cristin.projects.common;

import static no.unit.nva.cristin.model.QueryParameterKey.LANGUAGE;
import static no.unit.nva.cristin.model.QueryParameterKey.PAGE_CURRENT;
import static no.unit.nva.cristin.model.QueryParameterKey.PAGE_ITEMS_PER_PAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URI;

import no.unit.nva.cristin.model.QueryParameterKey;
import nva.commons.apigateway.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;

public class CristinQueryTest {

    private static final String APPROVAL_REFERENCE_ID = "2017/1593";
    private static final String APPROVED_BY = "REK";
    private static final String BIOBANK = "533895";
    private static final String FROM_PAGE = "2";
    private static final String GET_ONE_CRISTIN_PROJECT_EXAMPLE_URI =
        "https://api.cristin-test.uio.no/v2/projects/1234?lang=nb";
    private static final String ID = "1234";
    private static final String KEYWORD = "nature";
    private static final String LANGUAGE_NB = "nb";
    private static final String PARENT_UNIT_ID = "185.90.0.0";
    private static final String PER_PAGE = "10";
    private static final String QUERY_CRISTIN_PROJECTS_EXAMPLE_URI =
        "https://api.cristin-test.uio.no/v2/projects?lang=nb&page=2&parent_unit_id=185.90.0.0"
        + "&per_page=10&title=reindeer";
    private static final String QUERY_SAMPLE_WITH_MULTIPLE_PARAMETERS =
        "https://api.cristin-test.uio.no/v2/projects?approval_reference_id=2017/1593&approved_by=REK&biobank=533895"
        + "&biobank=533895&funding_source=NFR&institution=uib&keyword=nature&keyword=nature&lang=nb&levels=7&page=1"
        + "&participant=St&participant=St&per_page=5&project_manager=st&sort=start_date&unit=184.12.60.0";
    private static final String RANDOM_TITLE = "reindeer";
    private static final String SAMPLE_FUNDING_SOURCE = "NFR";
    private static final String SAMPLE_INSTITUTION_ID = "uib";
    private static final String SAMPLE_LEVELS = "7";
    private static final String SAMPLE_PARTICIPANT = "St";
    private static final String SAMPLE_PROJECT_MANAGER = "st";
    private static final String SAMPLE_SORT = "start_date";
    private static final String SAMPLE_UNIT = "184.12.60.0";
    public static final QueryParameterKey[] QUERY_PARAMETER_KEYS = {PAGE_CURRENT, PAGE_ITEMS_PER_PAGE, LANGUAGE};

    @Test
    void buildReturnsUriWithCustomParameterValuesWhenCustomParameterValuesAreSupplied() throws BadRequestException {
        var cristinQuery =
            CristinQuery.builder()
                .withTitle(RANDOM_TITLE)
                .withLanguage(LANGUAGE_NB)
                .withItemsPerPage(PER_PAGE)
                .withItemsFromPage(FROM_PAGE)
                .withParentUnitId(PARENT_UNIT_ID)
                .withRequiredParameters(QUERY_PARAMETER_KEYS)
                .validate().build();
        var uriString = cristinQuery.toURI().toString();
        assertEquals(QUERY_CRISTIN_PROJECTS_EXAMPLE_URI, uriString);
    }

    @Test
    void buildReturnsUriWithIdAndLanguageWhenIdAndLanguageParametersAreSupplied() throws BadRequestException {
        URI uri = CristinQuery.fromIdAndLanguage(ID, LANGUAGE_NB);
        var uri2 = CristinQuery.builder()
                       .withPathIdentity(ID)
                       .withLanguage(LANGUAGE_NB)
                       .validate().build().toURI();
        assertEquals(GET_ONE_CRISTIN_PROJECT_EXAMPLE_URI, uri.toString());
        assertEquals(GET_ONE_CRISTIN_PROJECT_EXAMPLE_URI, uri2.toString());
    }

    @Test
    void buildReturnsUriWithExtendedListOfParameters() throws BadRequestException {
        URI uri =
            CristinQuery.builder()
                .withItemSort(SAMPLE_SORT)
                .withInstitution(SAMPLE_INSTITUTION_ID)
                .withProjectManager(SAMPLE_PROJECT_MANAGER)
                .withParticipant(SAMPLE_PARTICIPANT)
                .withParticipant(SAMPLE_PARTICIPANT)
                .withFundingSource(SAMPLE_FUNDING_SOURCE)
                .withUnit(SAMPLE_UNIT)
                .withLevels(SAMPLE_LEVELS)
                .withKeyword(KEYWORD)
                .withKeyword(KEYWORD)
                .withBiobank(BIOBANK)
                .withBiobank(BIOBANK)
                .withApprovedBy(APPROVED_BY)
                .withApprovalReferenceId(APPROVAL_REFERENCE_ID)
                .withRequiredParameters(QUERY_PARAMETER_KEYS)
                .validate().build()
                .toURI();
        assertEquals(QUERY_SAMPLE_WITH_MULTIPLE_PARAMETERS, uri.toString());
    }
}
