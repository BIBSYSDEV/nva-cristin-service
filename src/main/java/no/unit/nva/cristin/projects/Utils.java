package no.unit.nva.cristin.projects;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

public class Utils {

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
}
