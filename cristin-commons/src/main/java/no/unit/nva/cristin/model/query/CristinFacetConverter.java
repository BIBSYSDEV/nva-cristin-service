package no.unit.nva.cristin.model.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import no.unit.nva.model.Facet;
import no.unit.nva.model.FacetConverter;

public class CristinFacetConverter implements FacetConverter {

    private final Map<String, List<Facet>> converted;

    public CristinFacetConverter() {
        converted = new HashMap<>();
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
        return toFacets(input.get(cristinFacetKey));
    }

    private List<Facet> toFacets(CristinFacet... cristinFacets) {
        return Arrays.stream(cristinFacets).map(this::toFacet).toList();
    }

    private Facet toFacet(CristinFacet cristinFacet) {
        return new CristinFacetAdapter(cristinFacet);
    }

    @Override
    public Map<String, List<Facet>> getConverted() {
        return converted;
    }

}
