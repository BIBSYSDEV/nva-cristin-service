package no.unit.nva.utils;

import org.apache.http.entity.ContentType;

public class VersioningUtils {

    public static final String VERSION = "version";

    /**
     * Extract the version field value if present from a header or else null.
     */
    public static String extractVersion(String headerValue) {
        var contentType = ContentType.parse(headerValue);

        return contentType.getParameter(VERSION);
    }

}
