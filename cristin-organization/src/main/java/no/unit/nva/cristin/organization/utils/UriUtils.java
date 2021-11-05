package no.unit.nva.cristin.organization.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public final class UriUtils {

    public static final String QUERY_PARAM_LANGUAGE = "lang";
    public static final Logger logger = LoggerFactory.getLogger(UriUtils.class);
    public static final String UNEXPECTED_ERROR_WHILE_CLEARING_PARAMETERS =
        "Clearing parameters from valid URI resulted in URISyntaxException: ";
    private static final String UNEXPECTED_ERROR_WHILE_ADDING_LANGUAGE_PARAMETER =
        "Adding parameters to a valid URI resulted in URISyntaxError: ";

    private UriUtils() {
    }

    /**
     * Add the language parameter in the Request URI.
     *
     * @param uri      the Institution or Unit URI.
     * @param language the language code ("en", "nb", "nn").
     * @return the URI with the Language parameter
     */
    public static URI getUriWithLanguage(URI uri, Language language) {
            return uri;
    }

    /**
     * Clears parameters from URI.
     *
     * @param uri URI with or without parameters.
     * @return a URI without parameters
     */
    public static URI clearParameters(URI uri) {
        return uri;
    }
}
