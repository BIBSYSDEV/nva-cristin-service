package no.unit.nva.cristin.model.query;

import java.util.Arrays;
import java.util.Optional;
import no.unit.nva.facet.FacetKey;
import nva.commons.core.SingletonCollector;

public enum CristinFacetKey implements FacetKey {

    INSTITUTION("institution_idfacet", "organizationFacet"),
    SECTOR("sector_idfacet", "sectorFacet");

    private final String cristinKey;
    private final String nvaKey;

    CristinFacetKey(String cristinKey, String nvaKey) {
        this.cristinKey = cristinKey;
        this.nvaKey = nvaKey;
    }

    @Override
    public String getKey() {
        return cristinKey;
    }

    @Override
    public String getNvaKey() {
        return nvaKey;
    }

    public static Optional<String> getNvaKey(String key) {
        return Optional.ofNullable(Arrays.stream(values())
                                       .filter(facetKey -> facetKey.getKey().equals(key))
                                       .map(CristinFacetKey::getNvaKey)
                                       .collect(SingletonCollector.collectOrElse(null)));
    }

    public static Optional<String> getCristinKey(String key) {
        return Optional.ofNullable(Arrays.stream(values())
                                       .filter(facetKey -> facetKey.getNvaKey().equals(key))
                                       .map(CristinFacetKey::getKey)
                                       .collect(SingletonCollector.collectOrElse(null)));
    }

    public static Optional<CristinFacetKey> fromKey(String key) {
        return Optional.ofNullable(Arrays.stream(values())
                   .filter(facetKey -> facetKey.getKey().equals(key) || facetKey.getNvaKey().equals(key))
                   .collect(SingletonCollector.collectOrElse(null)));
    }

    public static Optional<CristinFacetKey> fromCristinFacet(CristinFacet cristinFacet) {
        if (cristinFacet instanceof CristinSectorFacet) {
            return Optional.of(CristinFacetKey.SECTOR);
        } else if (cristinFacet instanceof CristinInstitutionFacet) {
            return Optional.of(CristinFacetKey.INSTITUTION);
        } else {
            return Optional.empty();
        }
    }

}
