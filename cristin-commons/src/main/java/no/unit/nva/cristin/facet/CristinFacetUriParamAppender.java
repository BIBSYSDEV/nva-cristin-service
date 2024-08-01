package no.unit.nva.cristin.facet;

import static java.util.Objects.nonNull;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import no.unit.nva.cristin.model.query.CristinFacetParamKey;
import nva.commons.core.paths.UriWrapper;

public class CristinFacetUriParamAppender {

    public static final String PARAM_VALUE_DELIMITER = ",|%2C";
    private UriWrapper appendedCristinUri;

    /**
     * Takes a cristin uri and a query parameter map as an input. If query param map contains any facets, they will be
     * appended to the cristin uri. If it is comma separated values, they will be split and the param key will be
     * added multiple times, one for each value.
     */
    public CristinFacetUriParamAppender(URI originalCristinUri, Map<String, String> facetParams) {
        this.appendedCristinUri = UriWrapper.fromUri(originalCristinUri);
        if (nonNull(facetParams)) {
            facetParams.forEach(this::addParam);
        }
    }

    private void addParam(String key, String value) {
        var paramEnum = CristinFacetParamKey.fromKey(key);
        paramEnum.ifPresent(
            param -> splitOnComma(value).forEach(valuePart -> appendParamToUri(param.getKey(), valuePart))
        );
    }

    private List<String> splitOnComma(String value) {
        return Optional.ofNullable(value)
                   .map(str -> str.split(PARAM_VALUE_DELIMITER))
                   .map(Arrays::asList)
                   .orElse(Collections.emptyList());
    }

    private void appendParamToUri(String key, String value) {
        appendedCristinUri = appendedCristinUri.addQueryParameter(key, value);
    }

    public UriWrapper getAppendedUri() {
        return appendedCristinUri;
    }

}
