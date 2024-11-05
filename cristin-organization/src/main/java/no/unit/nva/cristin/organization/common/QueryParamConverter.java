package no.unit.nva.cristin.organization.common;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.model.Constants.CRISTIN_PER_PAGE_PARAM;
import static no.unit.nva.cristin.model.Constants.CRISTIN_QUERY_NAME_PARAM;
import static no.unit.nva.cristin.model.Constants.SORT;
import static no.unit.nva.cristin.model.Constants.TOP;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueryParamConverter {

    public static final String CRISTIN_LEVELS_PARAM = "levels";
    public static final String FIRST_LEVEL = "1";
    public static final String ALL_SUB_LEVELS = "32";

    /**
     * Transforms request query params to cristin format for query params.
     */
    public static Map<String, String> translateToCristinApi(Map<String, String> requestQueryParams) {
        var translatedParams = new ConcurrentHashMap<String, String>();

        if (requestQueryParams.containsKey(QUERY)) {
            translatedParams.put(CRISTIN_QUERY_NAME_PARAM, requestQueryParams.get(QUERY));
        }

        translatedParams.put(CRISTIN_LEVELS_PARAM, toCristinLevel(requestQueryParams.get(DEPTH)));
        translatedParams.put(PAGE, requestQueryParams.get(PAGE));
        translatedParams.put(CRISTIN_PER_PAGE_PARAM, requestQueryParams.get(NUMBER_OF_RESULTS));

        if (requestQueryParams.containsKey(SORT)) {
            var sort = requestQueryParams.get(SORT);
            var sortConverted = convertSortValues(sort);
            translatedParams.put(SORT, sortConverted);
        }

        return translatedParams;
    }

    private static String toCristinLevel(String depth) {
        return TOP.equals(depth) || isNull(depth) ? FIRST_LEVEL : ALL_SUB_LEVELS;
    }

    private static String convertSortValues(String input) {
        return input.replace("nameNb", "name_nb")
                   .replace("nameEn", "name_en")
                   .replace("nameNn", "name_nn");
    }

}
