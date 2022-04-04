package no.unit.nva.cristin.projects;

import nva.commons.core.JacocoGenerated;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public enum ProjectStatus {
    NOTSTARTED("not started"),
    ACTIVE("active"),
    CONCLUDED("concluded");

    public static final String ERROR_MESSAGE_TEMPLATE = "%s not a valid ProjectStatus, expected one of: %s";
    public static final String DELIMITER = ", ";

    private final String cristinStatus;

    ProjectStatus(String cristinStatus) {
        this.cristinStatus = cristinStatus;
    }

    /**
     * Lookup ProjectStatus by string from cristin value.
     *
     * @param name assumed status name
     * @return enum value associated with name
     */
    public static ProjectStatus fromCristinStatus(String name) {
        return stream(values())
                .filter(nameType -> nameType.getCristinStatus().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> returnException(name));
    }

    public static boolean isValidStatus(String nameCandidate) {
        return stream(values()).anyMatch(enumName -> isValidAnyStatus(nameCandidate, enumName));
    }

    private static boolean isValidAnyStatus(String nameCandidate, ProjectStatus enumName) {
        return enumName.getCristinStatus().equalsIgnoreCase(nameCandidate) || enumName.name().equalsIgnoreCase(nameCandidate);
    }

    @JacocoGenerated
    private static RuntimeException returnException(String name) {
        return new IllegalArgumentException(
                format(ERROR_MESSAGE_TEMPLATE, name, stream(ProjectStatus.values())
                        .map(ProjectStatus::toString).collect(joining(DELIMITER))));
    }

    /**
     * Maps to a ProjectStatus from given string.
     *
     * @param name string to map
     * @return corresponding ProjectStatus in NVA
     */
    public static ProjectStatus getNvaStatus(String name) {
        return stream(values())
                .filter(nameType -> nameType.name().equalsIgnoreCase(name))
                .findAny().get();
    }

    public String getCristinStatus() {
        return cristinStatus;
    }
}
