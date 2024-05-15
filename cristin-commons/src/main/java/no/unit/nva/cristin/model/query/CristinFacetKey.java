package no.unit.nva.cristin.model.query;

import java.util.Arrays;
import java.util.Optional;
import no.unit.nva.facet.FacetKey;
import nva.commons.core.SingletonCollector;

public enum CristinFacetKey implements FacetKey {

    INSTITUTION("institution_idfacet", "organizationFacet"),
    SECTOR("sector_idfacet", "sectorFacet"),
    COORDINATING("institution_coordinating_idfacet", "coordinatingFacet"),
    RESPONSIBLE("institution_responsible_idfacet", "responsibleFacet"),
    CATEGORY("category_idfacet", "categoryFacet"),
    HEALTH("health_project_type_idfacet", "healthProjectFacet"),
    PARTICIPANT("person_idfacet", "participantFacet"),
    PARTICIPATING_PERSON_ORG("person_institution_idfacet", "participantOrgFacet"),
    FUNDING_SOURCE("funding_source_idfacet", "fundingSourceFacet");

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

}
