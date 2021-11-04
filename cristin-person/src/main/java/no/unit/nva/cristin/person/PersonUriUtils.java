package no.unit.nva.cristin.person;

import static no.unit.nva.cristin.common.util.UriUtils.SLASH_DELIMITER;
import static no.unit.nva.cristin.common.util.UriUtils.queryParameters;
import static no.unit.nva.cristin.person.Constants.BASE_PATH;
import static no.unit.nva.cristin.person.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.person.Constants.HTTPS;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.paths.UriWrapper.EMPTY_FRAGMENT;
import java.net.URI;
import java.util.Map;
import nva.commons.core.JacocoGenerated;

// TODO: Can these methods be shared by both Person and Project?
@JacocoGenerated // TODO: Will be tested later
public class PersonUriUtils {

    private static final String PERSON = "person";

    public static URI getPersonUriWithParams(Map<String, String> parameters) {
        return attempt(() -> new URI(HTTPS, DOMAIN_NAME, getCommonPath(),
            queryParameters(parameters), EMPTY_FRAGMENT)).orElseThrow();
    }

    public static URI getPersonUriWithId(String id) {
        return attempt(() -> new URI(HTTPS, DOMAIN_NAME, getCommonPath() + id,
            EMPTY_FRAGMENT)).orElseThrow();
    }

    private static String getCommonPath() {
        return SLASH_DELIMITER + BASE_PATH + SLASH_DELIMITER + PERSON + SLASH_DELIMITER;
    }
}
