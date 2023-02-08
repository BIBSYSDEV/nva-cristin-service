package no.unit.nva.cristin.projects.model.nva;

import static java.util.Arrays.stream;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Collectors;
import nva.commons.core.SingletonCollector;

public enum ClinicalTrialPhase {

    PHASE_ONE("PhaseI"),
    PHASE_TWO("PhaseII"),
    PHASE_THREE("PhaseIII"),
    PHASE_FOUR("PhaseIV");

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
                   .collect(SingletonCollector.tryCollect())
                   .orElseThrow(failure -> new IllegalArgumentException(constructError()));
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
                   .collect(SingletonCollector.collectOrElse(null));
    }
}
