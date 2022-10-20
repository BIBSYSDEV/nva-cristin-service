package no.unit.nva.utils;

import nva.commons.apigateway.RequestInfo;

public class LogUtils {

    public static final String LOG_IDENTIFIERS = "Client has Cristin identifier {} from organization {}";
    public static final String NOT_PRESENT = "not present";

    /**
     * Extracts organization identifier from requestInfo or else default value
     */
    public static String extractOrgIdentifier(RequestInfo requestInfo) {
        try {
            return requestInfo
                       .getTopLevelOrgCristinId()
                       .map(UriUtils::extractLastPathElement)
                       .orElse(NOT_PRESENT);
        } catch (Exception e) {
            return NOT_PRESENT;
        }
    }

    /**
     * Extracts cristin identifier from requestInfo or else default value
     */
    public static String extractCristinIdentifier(RequestInfo requestInfo) {
        try {
            return UriUtils.extractLastPathElement(requestInfo.getPersonCristinId());
        } catch (Exception e) {
            return NOT_PRESENT;
        }
    }

}
