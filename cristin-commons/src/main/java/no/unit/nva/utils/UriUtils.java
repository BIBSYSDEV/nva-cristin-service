package no.unit.nva.utils;

import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import nva.commons.core.paths.UriWrapper;

public class UriUtils {

    public static final String PROJECT = "project";
    public static final String INSTITUTION = "institution";
    public static final String WHITESPACE_REGEX = "\\s+";
    public static final String WHITESPACE_REPLACEMENT = "+";
    private static final String PARAMETER_KEY_VALUE_PAIR_TEMPLATE = "%s=%s";
    private static final String PARAMETER_DELIMITER = "&";
    private static final String EMPTY_QUERY_PARAMETERS_FOR_URI_CONSTRUCTOR = null;


    public static String escapeWhiteSpace(String text) {
        return text.replaceAll(WHITESPACE_REGEX, WHITESPACE_REPLACEMENT);
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
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> String.format(PARAMETER_KEY_VALUE_PAIR_TEMPLATE, entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(PARAMETER_DELIMITER));
    }

    public static URI createIdUri(String id, String type) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(type).addChild(id).getUri();
    }

    public static URI createIdUriFromParams(Map<String, String> requestQueryParams, String type) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(type)
            .addQueryParameters(requestQueryParams).getUri();
    }
}
