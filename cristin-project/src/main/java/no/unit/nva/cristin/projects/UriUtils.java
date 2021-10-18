package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.Constants.BASE_PATH;
import static no.unit.nva.cristin.projects.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.projects.Constants.EMPTY_FRAGMENT;
import static no.unit.nva.cristin.projects.Constants.HTTPS;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

public class UriUtils {

    private static final String SLASH_DELIMITER = "/";
    private static final String PARAMETER_KEY_VALUE_PAIR_TEMPLATE = "%s=%s";
    private static final String PARAMETER_DELIMITER = "&";
    private static final String EMPTY_QUERY_PARAMETERS_FOR_URI_CONSTRUCTOR = null;
    public static final String WHITESPACE_REGEX = "\\s+";
    public static final String WHITESPACE_REPLACEMENT = "+";

    public static String escapeWhiteSpace(String text) {
        return text.replaceAll(WHITESPACE_REGEX, WHITESPACE_REPLACEMENT);
    }

    public static URI buildUri(String... parts) {
        return attempt(() -> new URI(String.join(SLASH_DELIMITER, parts))).orElseThrow();
    }

    public static URI getNvaProjectUriWithParams(Map<String, String> parameters) {
        return attempt(() -> new URI(HTTPS, DOMAIN_NAME, SLASH_DELIMITER + BASE_PATH + SLASH_DELIMITER,
            queryParameters(parameters), EMPTY_FRAGMENT)).orElseThrow();
    }

    public static URI getNvaProjectUriWithId(String id) {
        return attempt(() -> new URI(HTTPS, DOMAIN_NAME, SLASH_DELIMITER + BASE_PATH + SLASH_DELIMITER + id,
            EMPTY_FRAGMENT)).orElseThrow();
    }

    /**
     * Creates as string from a map containing a formatted query parameters string to be put on end of an url.
     *
     * @param queryParameters The query params
     * @return a String with query parameters formatted to be placed at end of url
     */
    public static String queryParameters(Map<String, String> queryParameters) {
        return Optional.ofNullable(queryParameters)
            .map(UriUtils::formatQueryParameters)
            .orElse(EMPTY_QUERY_PARAMETERS_FOR_URI_CONSTRUCTOR);
    }

    private static String formatQueryParameters(Map<String, String> queryParams) {
        return queryParams.entrySet().stream()
            .sorted(Entry.comparingByKey())
            .map(entry -> String.format(PARAMETER_KEY_VALUE_PAIR_TEMPLATE, entry.getKey(), entry.getValue()))
            .collect(Collectors.joining(PARAMETER_DELIMITER));
    }
}
