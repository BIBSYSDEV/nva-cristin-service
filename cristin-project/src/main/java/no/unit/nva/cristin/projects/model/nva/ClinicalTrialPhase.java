package no.unit.nva.cristin.projects.model.nva;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;
import nva.commons.apigateway.exceptions.BadRequestException;

public enum ClinicalTrialPhase {

    PHASE_ONE("1"),
    PHASE_TWO("2"),
    PHASE_THREE("3"),
    PHASE_FOUR("4"),
    INVALID_VALUE("INVALID_VALUE");

    public static final String ERROR_MESSAGE_TEMPLATE = "Supplied ClinicalTrialPhase is not valid, expected one of: %s";
    public static final String DELIMITER = ", ";

    private final String phase;

    ClinicalTrialPhase(String phase) {
        this.phase = phase;
    }

    @JsonValue
    public String getPhase() {
        return phase;
    }

    /**
     * Lookup ClinicalTrialPhase by json. If value not supported, default to value for invalid input.
     */
    @JsonCreator
    @SuppressWarnings("unused")
    public static ClinicalTrialPhase fromJson(String value) {
        return stream(values())
                   .filter(nameType -> nameType.getPhase().equalsIgnoreCase(value))
                   .findAny()
                   .orElse(ClinicalTrialPhase.INVALID_VALUE);
    }

    /**
     * Lookup ClinicalTrialPhase by value. If value not supported, returns null.
     */
    public static ClinicalTrialPhase fromValue(String value) {
        return stream(values())
                   .filter(nameType -> nameType.getPhase().equalsIgnoreCase(value))
                   .findAny()
                   .orElse(null);
    }

    /**
     * Check if value for invalid is the enum contained in the class.
     */
    public static boolean hasValueInvalid(ClinicalTrialPhase clinicalTrialPhase) {
        return Optional.ofNullable(clinicalTrialPhase)
                   .map(ClinicalTrialPhase::getPhase)
                   .filter(str -> ClinicalTrialPhase.INVALID_VALUE.getPhase().equals(str))
                   .isPresent();
    }

    /**
     * Generates a suitable exception for handling unsupported value.
     */
    public static BadRequestException valueNotFoundException() {
        return new BadRequestException(format(ERROR_MESSAGE_TEMPLATE,
                                              stream(ClinicalTrialPhase.values())
                                                  .filter(phase -> phase != ClinicalTrialPhase.INVALID_VALUE)
                                                  .map(ClinicalTrialPhase::getPhase)
                                                  .collect(joining(DELIMITER))));
    }
}
