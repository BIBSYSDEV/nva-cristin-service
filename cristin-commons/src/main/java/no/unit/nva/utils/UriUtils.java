package no.unit.nva.utils;

import static no.unit.nva.cristin.common.util.UriUtils.SLASH_DELIMITER;
import static no.unit.nva.cristin.common.util.UriUtils.queryParameters;
import static no.unit.nva.cristin.projects.Constants.BASE_PATH;
import static no.unit.nva.cristin.projects.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.projects.Constants.EMPTY_FRAGMENT;
import static no.unit.nva.cristin.projects.Constants.HTTPS;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.util.Map;

public class UriUtils {

    public static final String PROJECT = "project";
    public static final String INSTITUTION = "institution";

    public static URI createUriFromParams(Map<String, String> parameters, String module) {
        return attempt(() -> new URI(HTTPS, DOMAIN_NAME, getCommonPath(module),
            queryParameters(parameters), EMPTY_FRAGMENT)).orElseThrow();
    }

    public static URI getNvaProjectUriWithId(String id, String module) {
        return attempt(() -> new URI(HTTPS, DOMAIN_NAME, getCommonPath(module) + id,
            EMPTY_FRAGMENT)).orElseThrow();
    }

    private static String getCommonPath(String module) {
        return SLASH_DELIMITER + BASE_PATH + SLASH_DELIMITER + module + SLASH_DELIMITER;
    }


}
