package no.unit.nva.cristin.projects;

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

    public static String escapeWhiteSpace(String text) {
        return text.replaceAll(" +", "+");
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
            .sorted(Entry.comparingByKey())
            .map(entry -> String.format(PARAMETER_KEY_VALUE_PAIR_TEMPLATE, entry.getKey(), entry.getValue()))
            .collect(Collectors.joining(PARAMETER_DELIMITER));
    }
}
