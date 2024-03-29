package no.unit.nva.cristin.facet;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import no.unit.nva.cristin.model.query.CristinFacet;
import no.unit.nva.cristin.model.query.CristinFacetKey;
import no.unit.nva.facet.Facet;

public class CristinFacetConverter {

    private final Map<String, List<Facet>> converted;
    private final URI nvaIdUri;

    /**
     * Takes a facet map from Cristin and converts it to a facet map formatted for NVA. Unsupported facets are
     * silently ignored. Uses Nva id uri as input for calculating facet id uri.
     */
    public CristinFacetConverter(URI nvaIdUri) {
        converted = new HashMap<>();
        this.nvaIdUri = nvaIdUri;
    }

    public CristinFacetConverter convert(Map<String, CristinFacet[]> input) {
        Optional.ofNullable(input)
            .map(Map::keySet)
            .orElse(Collections.emptySet())
            .forEach(facetKey -> processAndAdd(input, facetKey));

        return this;
    }

    private void processAndAdd(Map<String, CristinFacet[]> input, String facetKey) {
        var mappedFacetEnum = mapToFacetEnum(facetKey);
        mappedFacetEnum.ifPresent(facetEnum -> addFacetToConverted(input, facetEnum));
    }

    private static Optional<CristinFacetKey> mapToFacetEnum(String facetKey) {
        return CristinFacetKey.fromKey(facetKey);
    }

    private void addFacetToConverted(Map<String, CristinFacet[]> input, CristinFacetKey facetEnum) {
        converted.put(facetEnum.getNvaKey(), convertCristinFacetHavingKey(input, facetEnum.getKey()));
    }

    private List<Facet> convertCristinFacetHavingKey(Map<String, CristinFacet[]> input, String cristinFacetKey) {
        return toFacets(cristinFacetKey, input.get(cristinFacetKey));
    }

    private List<Facet> toFacets(String cristinFacetKey, CristinFacet... cristinFacets) {
        return Arrays.stream(cristinFacets).map(cristinFacet -> toFacet(cristinFacetKey, cristinFacet)).toList();
    }

    private Facet toFacet(String cristinFacetKey, CristinFacet cristinFacet) {
        return new CristinFacetAdapter(cristinFacetKey, cristinFacet, nvaIdUri);
    }

    public Map<String, List<Facet>> getConverted() {
        return converted;
    }

}
