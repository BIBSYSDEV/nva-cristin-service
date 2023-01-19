package no.unit.nva.cristin.projects.model.nva;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import nva.commons.apigateway.exceptions.BadRequestException;

public enum ClinicalTrialPhase {

    PHASE_ONE("1"),
    PHASE_TWO("2"),
    PHASE_THREE("3"),
    PHASE_FOUR("4"),
    INVALID_VALUE("INVALID_VALUE");

    public static final String ERROR_MESSAGE_TEMPLATE = "%s not a valid ClinicalTrialPhase, expected one of: %s";
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
     * Generates a suitable exception for handling unsupported value.
     */
    public static BadRequestException valueNotFoundException(String value) {
        return new BadRequestException(format(ERROR_MESSAGE_TEMPLATE,
                                              value,
                                              stream(ClinicalTrialPhase.values())
                                                  .filter(phase -> phase != ClinicalTrialPhase.INVALID_VALUE)
                                                  .map(ClinicalTrialPhase::getPhase)
                                                  .collect(joining(DELIMITER))));
    }
}
