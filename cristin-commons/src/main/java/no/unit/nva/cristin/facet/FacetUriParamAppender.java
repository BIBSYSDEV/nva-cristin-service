package no.unit.nva.cristin.facet;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import no.unit.nva.cristin.model.query.CristinFacet;
import no.unit.nva.cristin.model.query.CristinFacetKey;
import nva.commons.core.paths.UriWrapper;

public class FacetUriParamAppender {

    public static final String QUERY_PARAMETER_DELIMITER = "&";
    public static final String QUERY_PARAMETER_ASSIGNER = "=";
    public static final String QUERY_VALUE_DELIMITER = ",";
    public static final List<String> KEYS_THAT_SHOULD_NOT_BE_SPLIT = List.of("title", "query", "name");
    private final URI nvaUri;
    private final String cristinFacetKey;
    private final CristinFacet cristinFacet;
    private UriWrapper uriWithFacetKeys;

    /**
     * Adds the facet name and facet key from input facet as a query param to the input nva uri. Facet name is linked to
     * the enum for Cristin facets. If the facet is already present, it adds the new one to the existing query param as
     * a comma separated list of facet keys. Returns a new uri with the added facet data.
     */
    public FacetUriParamAppender(URI nvaUri, String cristinFacetKey, CristinFacet cristinFacet) {
        this.nvaUri = nvaUri;
        this.cristinFacetKey = cristinFacetKey;
        this.cristinFacet = cristinFacet;
        if (nonNull(nvaUri)) {
            uriWithFacetKeys = UriWrapper.fromUri(nvaUri);
        }
    }

    public FacetUriParamAppender create() {
        if (isNull(cristinFacet) || isNull(nvaUri)) {
            return this;
        }

        var facetEnum = getCorrectFacetEnumFromCristinFacet();

        var queryParam = nvaUri.getQuery();
        var queryParamMap = nonNull(queryParam) ? getQueryMap(queryParam) : new HashMap<String, List<String>>();

        var facetName = facetEnum.getNvaKey();
        var facetContentKey = cristinFacet.getKey();

        if (queryParamMap.containsKey(facetName)) {
            queryParamMap.get(facetName).add(facetContentKey);
        } else {
            queryParamMap.put(facetName, Collections.singletonList(facetContentKey));
        }

        var combinedMap = getCombinedMap(queryParamMap);

        uriWithFacetKeys = uriWithNewParams(combinedMap);

        return this;
    }

    public Optional<UriWrapper> getUriWithFacetKeys() {
        return Optional.ofNullable(uriWithFacetKeys);
    }

    private static List<String> splitOnComma(String value) {
        return Arrays.asList(value.split(QUERY_VALUE_DELIMITER));
    }

    private static List<String> keepValueAsIs(String value) {
        return Collections.singletonList(value);
    }

    private CristinFacetKey getCorrectFacetEnumFromCristinFacet() {
        return CristinFacetKey.fromKey(cristinFacetKey).orElseThrow();
    }

    private Map<String, List<String>> getQueryMap(String query) {
        var parametersMap = new HashMap<String, List<String>>();
        var fragments = query.split(QUERY_PARAMETER_DELIMITER);

        var queryParameter = QueryParameter.empty();

        for (String fragment : fragments) {
            queryParameter = saveAndProcessNextFragment(fragment, queryParameter, parametersMap);
        }

        saveParameterIfExists(queryParameter, parametersMap);
        return parametersMap;
    }

    private QueryParameter saveAndProcessNextFragment(String fragment, QueryParameter currentParam,
                                                      Map<String, List<String>> parametersMap) {
        if (isKeyValuePair(fragment)) {
            saveParameterIfExists(currentParam, parametersMap);
            return QueryParameter.fromString(fragment);
        }
        return currentParam.withAppendedFragment(fragment);
    }

    private void saveParameterIfExists(QueryParameter param, Map<String, List<String>> map) {
        if (param.hasKey()) {
            addParameterToMap(map, param.key(), param.value());
        }
    }

    private static boolean isKeyValuePair(String fragment) {
        return fragment.contains(QUERY_PARAMETER_ASSIGNER);
    }

    private void addParameterToMap(Map<String, List<String>> map, String key, String value) {
        var values = shouldSplitOnComma(key) ? splitOnComma(value) : keepValueAsIs(value);
        map.put(key, new ArrayList<>(values));
    }

    private boolean shouldSplitOnComma(String keyFromParam) {
        return KEYS_THAT_SHOULD_NOT_BE_SPLIT.stream()
                   .noneMatch(keyNotToBeSplit -> keyNotToBeSplit.equals(keyFromParam));
    }

    private Map<String, String> getCombinedMap(Map<String, List<String>> queryMap) {
        var combinedMap = new TreeMap<String, String>();
        for (Map.Entry<String, List<String>> entry : queryMap.entrySet()) {
            var uniqueValues = new ArrayList<>(new HashSet<>(entry.getValue()));
            Collections.sort(uniqueValues);
            combinedMap.put(entry.getKey(), String.join(QUERY_VALUE_DELIMITER, uniqueValues));
        }

        return combinedMap;
    }

    private UriWrapper uriWithNewParams(Map<String, String> params) {
        var newUri = attempt(
            () -> new URI(nvaUri.getScheme(), nvaUri.getHost(), nvaUri.getPath(), nvaUri.getFragment())).orElseThrow();

        return UriWrapper.fromUri(newUri).addQueryParameters(params);
    }

    public record QueryParameter(String key, String value) {

        public static QueryParameter fromString(String string) {
            var key = attempt(() -> string.split(QUERY_PARAMETER_ASSIGNER)[0]).orElse(failure -> null);
            var value = attempt(() -> string.split(QUERY_PARAMETER_ASSIGNER)[1]).orElse(failure -> null);
            return new QueryParameter(key, value);
        }

        public static QueryParameter empty() {
            return new QueryParameter(null, EMPTY_STRING);
        }

        public QueryParameter withAppendedFragment(String fragment) {
            var delimiter = hasNoValue() ? EMPTY_STRING : QUERY_PARAMETER_DELIMITER;
            var value = "%s%s%s".formatted(value(), delimiter, fragment);
            return new QueryParameter(key(), value);
        }

        private boolean hasNoValue() {
            return isNull(value) || value.isBlank();
        }

        public boolean hasKey() {
            return nonNull(key);
        }
    }
}
