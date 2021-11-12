package no.unit.nva.utils;

import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.util.Map;

import static no.unit.nva.cristin.common.util.UriUtils.SLASH_DELIMITER;
import static no.unit.nva.cristin.common.util.UriUtils.queryParameters;
import static no.unit.nva.cristin.projects.Constants.BASE_PATH;
import static no.unit.nva.cristin.projects.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.projects.Constants.EMPTY_FRAGMENT;
import static no.unit.nva.cristin.projects.Constants.HTTPS;
import static nva.commons.core.attempt.Try.attempt;

public class UriUtils {

    public static final String PROJECT = "project";
    public static final String INSTITUTION = "institution";

    public static URI createUriFromParams(Map<String, String> parameters, String module) {
        return attempt(() -> new URI(HTTPS, DOMAIN_NAME, getCommonPath(module),
            queryParameters(parameters), EMPTY_FRAGMENT)).orElseThrow();
    }

    public static URI getNvaProjectUriWithId(String id) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(PROJECT)
                .addChild(id).getUri();
    }

    private static String getCommonPath(String module) {
        return SLASH_DELIMITER + BASE_PATH + SLASH_DELIMITER + module + SLASH_DELIMITER;
    }

}
