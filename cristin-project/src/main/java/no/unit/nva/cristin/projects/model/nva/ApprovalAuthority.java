package no.unit.nva.cristin.projects.model.nva;

import static java.util.Arrays.stream;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Collectors;
import nva.commons.core.SingletonCollector;

public enum ApprovalAuthority {

    REGIONAL_ETHICAL_COMMITTEES("RegionalEthicalCommittees"),
    NORWEGIAN_ANIMAL_RESEARCH_AUTHORITY("NorwegianAnimalResearchAuthority"),
    NORWEGIAN_DATA_PROTECTION_AUTHORITY("NorwegianDataProtectionAuthority"),
    NORWEGIAN_MEDICINES_AGENCY("NorwegianMedicinesAgency"),
    NORWEGIAN_DIRECTORATE_OF_HEALTH("NorwegianDirectorateOfHealth");

    public static final String ERROR_MESSAGE_TEMPLATE = "Supplied ApprovalAuthority is not valid expected one of: %s";
    public static final String DELIMITER = ", ";

    private final String authorityValue;

    ApprovalAuthority(String authorityValue) {
        this.authorityValue = authorityValue;
    }

    @JsonValue
    public String getAuthorityValue() {
        return authorityValue;
    }

    /**
     * Lookup ApprovalAuthority by json. If value not supported, throws exception.
     */
    @JsonCreator
    @SuppressWarnings("unused")
    public static ApprovalAuthority fromJson(String value) {
        return stream(values())
                   .filter(nameType -> nameType.getAuthorityValue().equalsIgnoreCase(value))
                   .collect(SingletonCollector.tryCollect())
                   .orElseThrow(failure -> new IllegalArgumentException(constructError()));
    }

    private static String constructError() {
        return String.format(ERROR_MESSAGE_TEMPLATE, collectValueString());
    }

    private static String collectValueString() {
        return stream(values()).map(ApprovalAuthority::getAuthorityValue).collect(Collectors.joining(DELIMITER));
    }

    /**
     * Lookup ApprovalAuthority by value. If value not supported, returns null.
     */
    public static ApprovalAuthority fromValue(String value) {
        return stream(values())
                   .filter(nameType -> nameType.getAuthorityValue().equalsIgnoreCase(value))
                   .collect(SingletonCollector.collectOrElse(null));
    }

}
