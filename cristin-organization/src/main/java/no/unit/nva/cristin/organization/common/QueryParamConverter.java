package no.unit.nva.cristin.organization.common;

import static no.unit.nva.cristin.model.Constants.CRISTIN_PER_PAGE_PARAM;
import static no.unit.nva.cristin.model.Constants.CRISTIN_QUERY_NAME_PARAM;
import static no.unit.nva.cristin.model.Constants.SORT;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueryParamConverter {

    /**
     * Transforms request query params to cristin format for query params.
     */
    public static Map<String, String> translateToCristinApi(Map<String, String> requestQueryParams) {
        var translatedParams = new ConcurrentHashMap<>(Map.of(
            CRISTIN_QUERY_NAME_PARAM, requestQueryParams.get(QUERY),
            PAGE, requestQueryParams.get(PAGE),
            CRISTIN_PER_PAGE_PARAM, requestQueryParams.get(NUMBER_OF_RESULTS)));

        if (requestQueryParams.containsKey(SORT)) {
            translatedParams.put(SORT, requestQueryParams.get(SORT));
        }

        return translatedParams;
    }

}
