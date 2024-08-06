package no.unit.nva.utils;

import java.util.Set;
import java.util.stream.Collectors;

public class SqlRecognizer {

    private static final Set<String> suspicousStatements = Set.of("''", "--", ";", "$", "=");

    /**
     * Checks if there are possible SQL statements in a string.
     *
     * @param query String to checked for SQL statements
     * @return true if there are possible SQL statements in str
     */
    public static boolean containsSqlInjectionFragmentCandidate(String query) {
        var intersect = suspicousStatements.stream().filter(query::contains).collect(Collectors.toSet());
        return !intersect.isEmpty();
    }
}
