package no.unit.nva.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class SqlRecognizer {

    private static final Set<String> sqlStatements = Set.of("'",
            "and",
            "exec",
            "insert",
            "select",
            "delete",
            "update",
            "create",
            "alter",
            "count",
            "drop",
            "chr",
            "mid",
            "master",
            "truncate",
            "char",
            "declare",
            ";",
            "or",
            "-",
            ",");

    /**
     * Checks if there are possible SQL statements in a string.
     * @param str String to checked for SQL statements
     * @return true if there are possible SQL statements in str
     */
    public static boolean containsSqlInjectionFragmentCandidate(String str) {
        Set<String> queryFragments  = new HashSet(List.of(str.toLowerCase(Locale.getDefault()).split(" ")));
        Set intersect = queryFragments.stream().filter(sqlStatements::contains).collect(Collectors.toSet());
        return !intersect.isEmpty();
    }
}
