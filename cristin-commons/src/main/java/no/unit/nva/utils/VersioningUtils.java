package no.unit.nva.utils;

import java.util.Optional;
import nva.commons.apigateway.RequestInfo;
import org.apache.hc.core5.http.ContentType;

public class VersioningUtils {

    public static final String VERSION = "version";
    public static final String ACCEPT_HEADER_KEY_NAME = "Accept";

    /**
     * Extract the version field value if present from a header or else null.
     */
    public static String extractVersion(String headerValue) {
        var contentType = ContentType.parse(headerValue);

        return contentType.getParameter(VERSION);
    }

    /**
     * Extract the version field value if present from header with key directly from RequestInfo or else null.
     */
    public static String extractVersionFromRequestInfo(RequestInfo requestInfo, String headerKey) {
        return Optional.of(requestInfo)
                   .map(RequestInfo::getHeaders)
                   .map(map -> map.get(headerKey))
                   .map(VersioningUtils::extractVersion)
                   .orElse(null);
    }

}
