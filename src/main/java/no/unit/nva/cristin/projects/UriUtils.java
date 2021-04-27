package no.unit.nva.cristin.projects;

import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UriUtils {

    public static URI buildUri(String... parts) {
        return attempt(() -> new URI(String.join("/", parts))).orElseThrow();
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
            .orElse(null);
    }

    private static String formatQueryParameters(Map<String, String> queryParams) {
        return queryParams.entrySet().stream()
            .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
            .collect(Collectors.joining("&"));
    }
}
