package no.unit.nva.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SqlRecognizer {

    private static final Set<String> suspicousStatements = Set.of("''", "--", ";","$","=");
    private static Pattern pattern = Pattern.compile("[\\p{Alnum}+\\s+-/–'&:();'#”«»´’_\"“`„‘’!^‐]+",Pattern.UNICODE_CHARACTER_CLASS);


//    private static final Set<String> sqlStatements = Set.of("'",
//            "and",
//            "exec",
//            "insert",
//            "select",
//            "delete",
//            "update",
//            "create",
//            "alter",
//            "count",
//            "drop",
//            "chr",
//            "mid",
//            "master",
//            "truncate",
//            "char",
//            "declare",
//            ";",
//            "or",
//            "-",
//            ",");
//
    /**
     * Checks if there are possible SQL statements in a string.
     * @param str String to checked for SQL statements
     * @return true if there are possible SQL statements in str
     */
    public static boolean containsSqlInjectionFragmentCandidate(String str) {
        Set<String> queryFragments  = new HashSet(List.of(str.toLowerCase(Locale.getDefault()).split(" ")));
        Set intersect = queryFragments.stream().filter(suspicousStatements::contains).collect(Collectors.toSet());
        return !intersect.isEmpty();
    }

    public static boolean containsSuspicousInjectionFragmentCandidate(String str) {
        Set<String> queryFragments  = new HashSet(List.of(str.toLowerCase(Locale.getDefault()).split("\\s")));
        Set intersect = queryFragments.stream().filter(suspicousStatements::contains).collect(Collectors.toSet());

        // |\\w|\\s+|'\\-'|'\\,','\\/','\\.')+"
//        Pattern pattern = Pattern.compile("([p{Alpha}]+|\\w+)+");
        final boolean onlyLegalCharacters = pattern.matcher(str).matches();
        final boolean empty = intersect.isEmpty();
        final boolean result = !empty || !onlyLegalCharacters;
        return result;
    }
}
