package no.unit.nva.utils;

import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.unit.nva.cristin.model.Constants.ALL_QUERY_PARAMETER_LANGUAGES;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_BASE;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.QUERY_PARAMETER_LANGUAGE;


public class UriUtils {

    public static final String PROJECT = "project";
    public static final String INSTITUTION = "institution";
    public static final String PERSON = "person";
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


    public static URI getNvaApiId(String identifier, String path) {
        return new UriWrapper(HTTPS,
                DOMAIN_NAME).addChild(BASE_PATH)
                .addChild(path)
                .addChild(identifier)
                .getUri();
    }

    public static URI getCristinUri(String identifier, String path) {
        return new UriWrapper(HTTPS, CRISTIN_API_BASE)
                .addChild(path)
                .addChild(identifier)
                .getUri();
    }

    public static URI addLanguage(URI uri) {
        return new UriWrapper(uri).addQueryParameter(QUERY_PARAMETER_LANGUAGE, ALL_QUERY_PARAMETER_LANGUAGES).getUri();
    }

    public static URI createIdUriFromParams(Map<String, String> requestQueryParams, String type) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(type)
            .addQueryParameters(requestQueryParams).getUri();
    }


    public static URI createCristinQueryUri(Map<String, String> cristinRequestQueryParams, String path) {
        URI queryUri = new UriWrapper(HTTPS, CRISTIN_API_BASE)
                .addChild(path)
                .addQueryParameters(cristinRequestQueryParams)
                .getUri();
        return queryUri;
    }
}
