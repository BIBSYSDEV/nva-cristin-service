package no.unit.nva.cristin.organization.common;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.model.Constants.CRISTIN_PER_PAGE_PARAM;
import static no.unit.nva.cristin.model.Constants.CRISTIN_QUERY_NAME_PARAM;
import static no.unit.nva.cristin.model.Constants.TOP;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import java.util.Map;

public class QueryParamConverter {

    public static final String CRISTIN_LEVELS_PARAM = "levels";
    public static final String FIRST_LEVEL = "1";
    public static final String ALL_SUB_LEVELS = "32";

    /**
     * Transforms request query params to cristin format for query params.
     */
    public static Map<String, String> translateToCristinApi(Map<String, String> requestQueryParams) {
        return Map.of(
            CRISTIN_LEVELS_PARAM, toCristinLevel(requestQueryParams.get(DEPTH)),
            CRISTIN_QUERY_NAME_PARAM, requestQueryParams.get(QUERY),
            PAGE, requestQueryParams.get(PAGE),
            CRISTIN_PER_PAGE_PARAM, requestQueryParams.get(NUMBER_OF_RESULTS));
    }

    private static String toCristinLevel(String depth) {
        return TOP.equals(depth) || isNull(depth) ? FIRST_LEVEL : ALL_SUB_LEVELS;
    }

}
