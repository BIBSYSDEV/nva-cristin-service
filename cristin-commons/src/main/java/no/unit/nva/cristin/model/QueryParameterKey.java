package no.unit.nva.cristin.model;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_NUMBER;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.common.ErrorMessages.INVALID_URI_MESSAGE;
import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessage;
import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum QueryParameterKey {
    INVALID(null),
    PATH_IDENTITY("identifier", null, Constants.PATTERN_IS_NON_EMPTY),
    PATH_ORGANISATION("parent_unit_id",
        "organization",
        ORGANIZATION_IDENTIFIER_PATTERN,
        ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS,
        true),
    PATH_PROJECT("projects","project",Constants.PATTERN_IS_NUMBER),
    BIOBANK("biobank"),
    FUNDING("funding"),
    FUNDING_SOURCE("funding_source"),
    GRANT_ID("project_code",null,Constants.PATTERN_IS_NUMBER),
    INSTITUTION("institution"),
    LANGUAGE("lang","language",Constants.PATTERN_IS_LANGUAGE),
    LEVELS("levels","depth",Constants.PATTERN_IS_NUMBER),
    NAME("name"),
    PROJECT_APPROVAL_REFERENCE_ID("approval_reference_id"),
    PROJECT_APPROVED_BY("approved_by"),
    PROJECT_KEYWORD("keyword"),
    PROJECT_ORGANIZATION("parent_unit_id",
        "organization",
        Constants.PATTERN_IS_URL,
        INVALID_URI_MESSAGE,
        true),
    PROJECT_MANAGER("project_manager"),
    PROJECT_MODIFIED_SINCE("modified_since",null,Constants.PATTERN_IS_DATE),
    PROJECT_PARTICIPANT("participant"),
    PROJECT_UNIT("unit"),
    QUERY("query",
        null,
        Constants.PATTERN_IS_NON_EMPTY,
        invalidQueryParametersMessage("query",
                                      ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE),
        true),
    STATUS("status", null, Constants.PATTERN_IS_STATUS, null, true),
    TITLE("title",
        null,
        Constants.PATTERN_IS_TITLE,
        invalidQueryParametersMessage("title",
                                      ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE),
        true),
    USER("user"),
    PAGE_CURRENT("page",
        null,
        Constants.PATTERN_IS_NUMBER,
        ERROR_MESSAGE_INVALID_NUMBER,
        false),
    PAGE_ITEMS_PER_PAGE("per_page",
        "results",
        Constants.PATTERN_IS_NUMBER,
        ERROR_MESSAGE_INVALID_NUMBER,
        false),
    PAGE_SORT("sort");

    public final static int IGNORE_PATH_PARAMETER_INDEX = 3;

    public static final Set<QueryParameterKey> VALID_QUERY_PARAMETERS =
        Arrays.stream(QueryParameterKey.values())
            .filter(QueryParameterKey::ignorePathKeys)
            .collect(Collectors.toSet());

    public static final Set<String> VALID_QUERY_PARAMETER_KEYS =
        VALID_QUERY_PARAMETERS.stream()
            .sorted()
            .map(QueryParameterKey::getKey)
            .collect(Collectors.toSet());

    public static final Set<String> VALID_QUERY_PARAMETER_NVA_KEYS =
        VALID_QUERY_PARAMETERS.stream()
            .sorted()
            .map(QueryParameterKey::getNvaKey)
            .collect(Collectors.toSet());

    private final String pattern;
    private final String cristinKey;
    private final String nvaKey;
    private final boolean encode;
    private final String errorMessage;

    QueryParameterKey(String cristinKey) {
        this(cristinKey, null, Constants.PATTERN_IS_NON_EMPTY, null, false);
    }

    QueryParameterKey(String cristinKey, String nvaKey, String pattern) {
        this(cristinKey, nvaKey, pattern, null, false);
    }

    QueryParameterKey(String cristinKey, String nvaKey, String pattern, String errorMessage,
                      boolean encode) {
        this.cristinKey = cristinKey;
        this.nvaKey = nonNull(nvaKey) ? nvaKey : cristinKey;
        this.pattern = pattern;
        this.encode = encode;
        this.errorMessage = errorMessage;
    }

    public String getNvaKey() {
        return nvaKey;
    }

    public String getKey() {
        return cristinKey;
    }

    public String getPattern() {
        return pattern;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isEncode() {
        return encode;
    }

    public static QueryParameterKey keyFromString(String paramName, String value) {
        var result = Arrays.stream(QueryParameterKey.values())
                         .filter(QueryParameterKey::ignorePathKeys)
                         .filter(equalTo(paramName))
                         .collect(Collectors.toSet());
        return result.size() == 1
                   ? result.stream().findFirst().get()
                   : result.stream()
                         .filter(hasValidValue(value))
                         .findFirst()
                         .orElse(INVALID);
    }

    private static Predicate<QueryParameterKey> hasValidValue(String value) {
        return f -> {
            var encoded = f.isEncode() ? URLDecoder.decode(value, StandardCharsets.UTF_8) : value;
            return encoded.matches(f.getPattern());
        };
    }

    private static Predicate<QueryParameterKey> equalTo(String name) {
        return key -> name.equals(key.getKey()) || name.equals(key.getNvaKey());
    }

    private static boolean ignorePathKeys(QueryParameterKey f) {
        return f.ordinal() > IGNORE_PATH_PARAMETER_INDEX;
    }

    public String getValue(Map<String, String> queryParams) {
        return queryParams.containsKey(getNvaKey())
                   ? queryParams.get(getNvaKey())
                   : queryParams.get(getKey());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", "Key[", "]")
                   .add(String.valueOf(ordinal()))
                   .add(name())
                   .toString();
    }

}
