package no.unit.nva.cristin.projects.model.nva;

import static java.util.Arrays.stream;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Collectors;

public enum HealthProjectType {

    DRUGSTUDY("DRUGSTUDY"),
    OTHERCLIN("OTHERCLIN"),
    OTHERSTUDY("OTHERSTUDY");

    public static final String ERROR_MESSAGE_TEMPLATE = "Supplied HealthProjectType is not valid, expected one of: %s";
    public static final String DELIMITER = ", ";

    private final String type;

    HealthProjectType(String type) {
        this.type = type;
    }

    @JsonValue
    public String getType() {
        return type;
    }

    /**
     * Lookup HealthProjectType by json. If value not supported, throws exception.
     */
    @JsonCreator
    @SuppressWarnings("unused")
    public static HealthProjectType fromJson(String value) {
        return stream(values())
                   .filter(nameType -> nameType.getType().equalsIgnoreCase(value))
                   .findAny()
                   .orElseThrow(() -> new IllegalArgumentException(constructError()));
    }

    private static String constructError() {
        return String.format(ERROR_MESSAGE_TEMPLATE, collectValueString());
    }

    private static String collectValueString() {
        return stream(values()).map(HealthProjectType::getType).collect(Collectors.joining(DELIMITER));
    }

    /**
     * Lookup HealthProjectType by value. If value not supported, returns null.
     */
    public static HealthProjectType fromValue(String value) {
        return stream(values())
                   .filter(nameType -> nameType.getType().equalsIgnoreCase(value))
                   .findAny()
                   .orElse(null);
    }

}
