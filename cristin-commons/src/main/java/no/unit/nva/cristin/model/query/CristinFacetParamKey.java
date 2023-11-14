package no.unit.nva.cristin.model.query;

import java.util.Arrays;
import java.util.Optional;
import no.unit.nva.facet.FacetKey;
import nva.commons.core.SingletonCollector;

public enum CristinFacetParamKey implements FacetKey {

    INSTITUTION_PARAM("institution", "organizationFacet"),
    SECTOR_PARAM("sector", "sectorFacet"),
    COORDINATING_PARAM("facet_coordinating", "coordinatingFacet"),
    RESPONSIBLE_PARAM("facet_responsible", "responsibleFacet"),
    CATEGORY_PARAM("category", "categoryFacet"),
    HEALTH_PARAM("facet_health", "healthProjectFacet"),
    PARTICIPANT_PARAM("participant", "participantFacet"),
    PARTICIPATING_PERSON_ORG_PARAM("facet_participating", "participantOrgFacet"),
    FUNDING_SOURCE_PARAM("funding_source", "fundingSourceFacet");


    private final String cristinKey;
    private final String nvaKey;

    CristinFacetParamKey(String cristinKey, String nvaKey) {
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
                                       .map(CristinFacetParamKey::getNvaKey)
                                       .collect(SingletonCollector.collectOrElse(null)));
    }

    public static Optional<String> getCristinKey(String key) {
        return Optional.ofNullable(Arrays.stream(values())
                                       .filter(facetKey -> facetKey.getNvaKey().equals(key))
                                       .map(CristinFacetParamKey::getKey)
                                       .collect(SingletonCollector.collectOrElse(null)));
    }

    public static Optional<CristinFacetParamKey> fromKey(String key) {
        return Optional.ofNullable(Arrays.stream(values())
                                       .filter(facetKey -> facetKey.getKey().equals(key)
                                                           || facetKey.getNvaKey().equals(key))
                                       .collect(SingletonCollector.collectOrElse(null)));
    }
}
