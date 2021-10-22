package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.common.util.UriUtils.SLASH_DELIMITER;
import static no.unit.nva.cristin.common.util.UriUtils.queryParameters;
import static no.unit.nva.cristin.projects.Constants.BASE_PATH;
import static no.unit.nva.cristin.projects.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.projects.Constants.EMPTY_FRAGMENT;
import static no.unit.nva.cristin.projects.Constants.HTTPS;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.util.Map;

public class ProjectUriUtils {

    public static URI getNvaProjectUriWithParams(Map<String, String> parameters) {
        return attempt(() -> new URI(HTTPS, DOMAIN_NAME, SLASH_DELIMITER + BASE_PATH + SLASH_DELIMITER,
            queryParameters(parameters), EMPTY_FRAGMENT)).orElseThrow();
    }

    public static URI getNvaProjectUriWithId(String id) {
        return attempt(() -> new URI(HTTPS, DOMAIN_NAME, SLASH_DELIMITER + BASE_PATH + SLASH_DELIMITER + id,
            EMPTY_FRAGMENT)).orElseThrow();
    }

}
