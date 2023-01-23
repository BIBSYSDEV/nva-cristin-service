package no.unit.nva.cristin.projects.model.nva;

import static java.util.Arrays.stream;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Collectors;

public enum ClinicalTrialPhase {

    PHASE_ONE("1"),
    PHASE_TWO("2"),
    PHASE_THREE("3"),
    PHASE_FOUR("4");

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
     * Lookup ClinicalTrialPhase by json. If value not supported, throws exception.
     */
    @JsonCreator
    @SuppressWarnings("unused")
    public static ClinicalTrialPhase fromJson(String value) {
        return stream(values())
                   .filter(nameType -> nameType.getPhase().equalsIgnoreCase(value))
                   .findAny()
                   .orElseThrow(() -> new IllegalArgumentException(constructError()));
    }

    private static String constructError() {
        return String.format(ERROR_MESSAGE_TEMPLATE, collectValueString());
    }

    private static String collectValueString() {
        return stream(values()).map(ClinicalTrialPhase::getPhase).collect(Collectors.joining(DELIMITER));
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
}
