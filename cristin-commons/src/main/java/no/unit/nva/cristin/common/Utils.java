package no.unit.nva.cristin.common;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import no.unit.nva.cristin.model.Constants;

import static java.util.Objects.nonNull;

public class Utils {

    public static final String PUNCTUATION = ".";

    /**
     * Check if a string supplied is a positive integer.
     *
     * @param str String to check
     * @return a boolean with value true if string is a positive integer or else false
     */
    public static boolean isPositiveInteger(String str) {
        try {
            int value = Integer.parseInt(str);
            return value > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static <K, V> Map<K, V> nonEmptyOrDefault(Map<K, V> map) {
        return nonNull(map) ? map : Collections.emptyMap();
    }

    public static <E> List<E> nonEmptyOrDefault(List<E> list) {
        return nonNull(list) ? list : Collections.emptyList();
    }

    public static <E> Set<E> nonEmptyOrDefault(Set<E> set) {
        return nonNull(set) ? set : Collections.emptySet();
    }

    public static boolean isOrcid(String identifier) {
        return Constants.ORCID_PATTERN.matcher(identifier).matches();
    }

    /**
     * A function that can be used to filter out duplicate values based on a given key.
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * Takes a Cristin unit identifier and converts it to its corresponding institution identifier.
     *
     * @return String with top level institution identifier
     */
    public static String removeUnitPartFromIdentifierIfPresent(String identifier) {
        if (nonNull(identifier) && identifier.contains(PUNCTUATION)) {
            return identifier.substring(0, identifier.indexOf(PUNCTUATION));
        }
        return identifier;
    }
}
