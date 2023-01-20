package no.unit.nva.cristin.projects.model.nva;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;
import nva.commons.apigateway.exceptions.BadRequestException;

public enum HealthProjectType {

    DRUGSTUDY("DRUGSTUDY"),
    OTHERCLIN("OTHERCLIN"),
    OTHERSTUDY("OTHERSTUDY"),
    INVALID_VALUE("INVALID_VALUE");

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
     * Lookup HealthProjectType by json. If value not supported, default to value for invalid input.
     */
    @JsonCreator
    @SuppressWarnings("unused")
    public static HealthProjectType fromJson(String value) {
        return stream(values())
                   .filter(nameType -> nameType.getType().equalsIgnoreCase(value))
                   .findAny()
                   .orElse(HealthProjectType.INVALID_VALUE);
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

    /**
     * Check if value for invalid is the enum contained in the class.
     */
    public static boolean hasValueInvalid(HealthProjectType healthProjectType) {
        return Optional.ofNullable(healthProjectType)
                   .map(HealthProjectType::getType)
                   .filter(str -> HealthProjectType.INVALID_VALUE.getType().equals(str))
                   .isPresent();
    }

    /**
     * Generates a suitable exception for handling unsupported value.
     */
    public static BadRequestException valueNotFoundException() {
        return new BadRequestException(format(ERROR_MESSAGE_TEMPLATE,
                                              stream(HealthProjectType.values())
                                                  .filter(type -> type != HealthProjectType.INVALID_VALUE)
                                                  .map(HealthProjectType::getType)
                                                  .collect(joining(DELIMITER))));
    }

}
