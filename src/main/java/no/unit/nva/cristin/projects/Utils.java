package no.unit.nva.cristin.projects;

import nva.commons.core.JacocoGenerated;

@JacocoGenerated
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
}
