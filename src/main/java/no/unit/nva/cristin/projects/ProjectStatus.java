package no.unit.nva.cristin.projects;

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
     * Lookup enum by value.
     *
     * @param name name
     * @return enum
     */
    public static ProjectStatus lookup(String name) {
        return stream(values())
                .filter(nameType -> nameType.name().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        format(ERROR_MESSAGE_TEMPLATE, name, stream(ProjectStatus.values())
                                .map(ProjectStatus::toString).collect(joining(DELIMITER)))));
    }
}
