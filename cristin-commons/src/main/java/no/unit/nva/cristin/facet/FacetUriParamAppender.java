package no.unit.nva.cristin.facet;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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
    private final URI nvaUri;
    private final CristinFacet cristinFacet;
    private UriWrapper uriWithFacetKeys;

    /**
     * Adds the facet name and facet key from input facet as a query param to the input nva uri. Facet name is linked
     * to the enum for Cristin facets. If the facet is already present, it adds the new one to the existing query
     * param as a comma separated list of facet keys. Returns a new uri with the added facet data.
     */
    public FacetUriParamAppender(URI nvaUri, CristinFacet cristinFacet) {
        this.nvaUri = nvaUri;
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

    private CristinFacetKey getCorrectFacetEnumFromCristinFacet() {
        return cristinFacet.getCristinFacetKey();
    }

    private Map<String, List<String>> getQueryMap(String query) {
        Map<String, List<String>> map = new HashMap<>();
        Arrays.stream(query.split(QUERY_PARAMETER_DELIMITER))
            .forEach(pair -> {
                String[] keyValue = pair.split(QUERY_PARAMETER_ASSIGNER);
                List<String> values = Arrays.asList(keyValue[1].split(QUERY_VALUE_DELIMITER));
                map.put(keyValue[0], new ArrayList<>(values));
            });
        return map;
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
        var newUri = attempt(() -> new URI(
            nvaUri.getScheme(),
            nvaUri.getHost(),
            nvaUri.getPath(),
            nvaUri.getFragment())
        ).orElseThrow();

        return UriWrapper.fromUri(newUri).addQueryParameters(params);
    }

}
