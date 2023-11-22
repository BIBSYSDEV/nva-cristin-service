package no.unit.nva.utils;

import java.util.Map;
import java.util.TreeMap;

public class CristinQueryUtils {

    public static final String CATEGORY = "category";
    public static final String CATEGORY_FACET = "categoryFacet";
    public static final String FUNDING_SOURCE = "funding_source";
    public static final String FUNDING_SOURCE_FACET = "fundingSourceFacet";
    public static final String PARTICIPANT = "participant";
    public static final String PARTICIPANT_FACET = "participantFacet";

    /**
     * Converts param keys that are the same as facet param keys but has another name into same names as facet param
     * keys. Useful when wanting to use a version that has support for facets but also has keys from another version
     * that don't support facets but has equal content.
     */
    public static Map<String, String> convertSupportedParamKeysToFacetParamKeys(Map<String, String> paramKeys) {
        var resultsWithConvertedKeys = new TreeMap<>(paramKeys);

        paramKeys.keySet().forEach(key -> {
            switch (key) {
                case CATEGORY -> {
                    resultsWithConvertedKeys.put(CATEGORY_FACET, paramKeys.get(key));
                    resultsWithConvertedKeys.remove(key);
                }
                case FUNDING_SOURCE -> {
                    resultsWithConvertedKeys.put(FUNDING_SOURCE_FACET, paramKeys.get(key));
                    resultsWithConvertedKeys.remove(key);
                }
                case PARTICIPANT -> {
                    resultsWithConvertedKeys.put(PARTICIPANT_FACET, paramKeys.get(key));
                    resultsWithConvertedKeys.remove(key);
                }
                default -> {
                    // do nothing
                }
            }
        });

        return resultsWithConvertedKeys;
    }

}
