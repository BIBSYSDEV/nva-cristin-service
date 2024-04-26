package no.unit.nva.cristin.person.model.cristin.adapter;

import java.util.Arrays;
import java.util.Optional;
import nva.commons.core.SingletonCollector;

public enum CristinAwardTypeMapper {

    RESEARCH("FORSKARBEID", "Research"),
    RESEARCH_DISSEMINATION("FORSKFORMIDL", "ResearchDissemination");

    private final String cristinKey;
    private final String nvaKey;

    CristinAwardTypeMapper(String cristinKey, String nvaKey) {
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
                                       .map(CristinAwardTypeMapper::getNvaKey)
                                       .collect(SingletonCollector.collectOrElse(null)));
    }

    public static Optional<String> getCristinKey(String key) {
        return Optional.ofNullable(Arrays.stream(values())
                                       .filter(typeKey -> typeKey.getNvaKey().equals(key))
                                       .map(CristinAwardTypeMapper::getKey)
                                       .collect(SingletonCollector.collectOrElse(null)));
    }

}
