package no.unit.nva.cristin.projects;

import nva.commons.core.JacocoGenerated;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public enum ProjectStatus {
    NOTSTARTED,
    ACTIVE,
    CONCLUDED;

    public static final String ERROR_MESSAGE_TEMPLATE = "%s not a valid ProjectStatus, expected one of: %s";
    public static final String DELIMITER = ", ";

    /**
     * Lookup project status by string value.
     *
     * @param name assumed status name
     * @return enum value associated with name
     */
    public static ProjectStatus lookup(String name) {
        return stream(values())
                .filter(nameType -> nameType.name().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> returnException(name));
    }

    public static boolean isValidStatus(String nameCandidate) {
        return stream(values()).anyMatch(enumName -> enumName.name().equalsIgnoreCase(nameCandidate));
    }

    @JacocoGenerated
    private static RuntimeException returnException(String name) {
        return new IllegalArgumentException(
                format(ERROR_MESSAGE_TEMPLATE, name, stream(ProjectStatus.values())
                        .map(ProjectStatus::toString).collect(joining(DELIMITER))));
    }
}
