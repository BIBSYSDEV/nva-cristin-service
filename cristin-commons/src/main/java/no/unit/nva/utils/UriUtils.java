package no.unit.nva.utils;

import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.paths.UriWrapper.EMPTY_FRAGMENT;


public class UriUtils {

    public static final String PROJECT = "project";
    public static final String INSTITUTION = "institution";
    public static final String SLASH_DELIMITER = "/";
    public static final String WHITESPACE_REGEX = "\\s+";
    public static final String WHITESPACE_REPLACEMENT = "+";
    private static final String PARAMETER_KEY_VALUE_PAIR_TEMPLATE = "%s=%s";
    private static final String PARAMETER_DELIMITER = "&";
    private static final String EMPTY_QUERY_PARAMETERS_FOR_URI_CONSTRUCTOR = null;


    public static String escapeWhiteSpace(String text) {
        return text.replaceAll(WHITESPACE_REGEX, WHITESPACE_REPLACEMENT);
    }

    public static URI buildUri(String... parts) {
        return attempt(() -> new URI(String.join(SLASH_DELIMITER, parts))).orElseThrow();
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

    /**
     * Creates a new URI from another URI using new query parameters.
     *
     * @param uri         The uri to be transformed
     * @param queryParams The query params
     * @return a URI from another URI but with new query parameters
     */
    public static URI getUriFromOtherUriUsingNewParams(URI uri, Map<String, String> queryParams) {
        return attempt(() ->
                new URI(uri.getScheme(), uri.getHost(), uri.getPath(), queryParameters(queryParams), EMPTY_FRAGMENT))
                .orElseThrow();
    }






    /**
     * Constructs a URI from NVA modle and a map containing parameters.
     * @param parameters map with parameters to add to uri
     * @param module which part of api is referenced
     * @return an valid URI containing module and parameters
     */
    public static URI createUriFromParams(Map<String, String> parameters, String module) {
        UriWrapper uriWrapper = new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(module);
        for (Map.Entry<String, String> e : parameters.entrySet()) {
            uriWrapper = uriWrapper.addQueryParameter(e.getKey(), e.getValue());
        }

        return uriWrapper.getUri();
    }

}
