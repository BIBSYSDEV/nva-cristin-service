package no.unit.nva.cristin.person.model.cristin.adapter;

import java.util.Arrays;
import java.util.Optional;
import nva.commons.core.SingletonCollector;

public enum CristinAwardDistributionMapper {

    INTERNATIONAL("INTERNAT", "International"),
    NATIONAL("NATIONAL", "National"),
    INTERNAL("INTERN", "Internal");

    private final String cristinKey;
    private final String nvaKey;

    CristinAwardDistributionMapper(String cristinKey, String nvaKey) {
        this.cristinKey = cristinKey;
        this.nvaKey = nvaKey;
    }

    public String getKey() {
        return cristinKey;
    }

    public String getNvaKey() {
        return nvaKey;
    }

    public static Optional<String> getNvaKey(String key) {
        return Optional.ofNullable(Arrays.stream(values())
                                       .filter(typeKey -> typeKey.getKey().equals(key))
                                       .map(CristinAwardDistributionMapper::getNvaKey)
                                       .collect(SingletonCollector.collectOrElse(null)));
    }

    public static Optional<String> getCristinKey(String key) {
        return Optional.ofNullable(Arrays.stream(values())
                                       .filter(typeKey -> typeKey.getNvaKey().equals(key))
                                       .map(CristinAwardDistributionMapper::getKey)
                                       .collect(SingletonCollector.collectOrElse(null)));
    }

}
