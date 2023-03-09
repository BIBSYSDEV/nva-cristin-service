package no.unit.nva.cristin.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import java.util.StringJoiner;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.apigateway.MediaTypes;
import nva.commons.core.Environment;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_NUMBER;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.common.ErrorMessages.INVALID_URI_MESSAGE;
import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessage;
import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;

public class Constants {

    public static final String X_TOTAL_COUNT = "x-total-count";
    public static final String LINK = "link";
    public static final String REL_NEXT = "rel=\"next\"";
    public static final String REL_PREV = "rel=\"prev\"";

    public static final ObjectMapper OBJECT_MAPPER = JsonUtils.dtoObjectMapper;
    public static final Environment ENVIRONMENT = new Environment();
    public static final String PROJECT_SEARCH_CONTEXT_URL =
        "https://bibsysdev.github.io/src/project-search-context.json";
    public static final String PROJECT_LOOKUP_CONTEXT_URL =
        "https://bibsysdev.github.io/src/project-context.json";
    public static final String HTTPS = "https";
    public static final String CRISTIN_API_URL = ENVIRONMENT.readEnv("CRISTIN_API_URL");

    public static final String INSTITUTION_PATH = "institutions";
    public static final String PERSON_PATH = "persons";
    public static final String PROJECTS_PATH = "projects";
    public static final String PROJECT_PATH_NVA = "project";

    public static final String BASE_PATH = ENVIRONMENT.readEnv("BASE_PATH");
    public static final String DOMAIN_NAME = ENVIRONMENT.readEnvOpt("DOMAIN_NAME")
        .orElse("api.dev.nva.aws.unit.no");
    public static final String FIRST_PAGE = "1";
    public static final String DEFAULT_NUMBER_OF_RESULTS = "5";

    public static final List<MediaType> DEFAULT_RESPONSE_MEDIA_TYPES = List.of(MediaType.JSON_UTF_8,
        MediaTypes.APPLICATION_JSON_LD);
    public static final String ORGANIZATION_PATH = "organization";
    public static final String PERSON_CONTEXT = "https://example.org/person-context.json";
    public static final String PERSON_PATH_NVA = "person";
    public static final String PERSON_QUERY_CONTEXT = "https://example.org/person-search-context.json";
    public static final String UNITS_PATH = "units";
    public static final String NOT_FOUND_MESSAGE_TEMPLATE = "The resource '%s' cannot be dereferenced";
    public static final String QUERY_PARAMETER_LANGUAGE = "lang";
    public static final String ALL_QUERY_PARAMETER_LANGUAGES = "en,nb,nn";
    public static final Pattern ORCID_PATTERN = Pattern.compile("[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{3}[0-9|Xx]");
    public static final String TOP = "top";
    public static final String FULL = "full";
    public static final String NONE = "none";
    public static final String PERSON_ID = "id";
    public static final String PERSONS_PATH = "persons";
    public static final String CRISTIN_QUERY_NAME_PARAM = "name";
    public static final String UNIT_ID = "id";
    public static final String EMPLOYMENT_ID = "employmentId";
    public static final String ORG_ID = "orgId";
    public static final String SORT = "sort";
    public static final String POSITION = "position";
    public static final String HASHTAG = "#";
    public static final String SLASH_DELIMITER = "/";
    public static final String CRISTIN_INSTITUTION_HEADER = "Cristin-Representing-Institution";
    public static final String CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME =
        ENVIRONMENT.readEnv("CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME");
    public static final String CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE =
        ENVIRONMENT.readEnv("CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE");

    private static final String PATTERN_IS_NUMBER6 = "[1-9]\\d*";
    public static final String PATTERN_IS_DATE = "(\\d){4}-(\\d){2}-(\\d){2}[T]*[(\\d){2}:(\\d){2}:(\\d){2,6}Z]*";
    private static final String PATTERN_IS_NON_EMPTY = ".+";
    private static final String PATTERN_IS_TITLE = "^[æøåÆØÅ\\w-,\\.: ]+$";
    private static final String PATTERN_IS_LANGUAGE = "(en|nb|nn|\\,)+";
    private static final String PATTERN_IS_STATUS = "(?i)CONCLUDED|ACTIVE|NOT[ +]*STARTED";
    public static final String PATTERN_IS_URL =
        "(http(s):\\/\\/.)[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";

    public enum QueryParameterKey {
        INVALID(null),
        PATH_IDENTITY("identifier", null, PATTERN_IS_NON_EMPTY),
        PATH_ORGANISATION(
            "parent_unit_id",
            "organization",
            ORGANIZATION_IDENTIFIER_PATTERN,
            ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS,
            true),
        PATH_PROJECT(
            "projects",
            "project",
            PATTERN_IS_NUMBER6),
        BIOBANK("biobank"),
        FUNDING("funding"),
        FUNDING_SOURCE("funding_source"),
        GRANT_ID(
            "project_code",
            null,
            PATTERN_IS_NUMBER6),
        INSTITUTION("institution"),
        LANGUAGE(
            "lang",
            "language",
            PATTERN_IS_LANGUAGE),
        LEVELS(
            "levels",
            "depth",
            PATTERN_IS_NUMBER6),
        NAME("name"),
        PROJECT_APPROVAL_REFERENCE_ID("approval_reference_id"),
        PROJECT_APPROVED_BY("approved_by"),
        PROJECT_KEYWORD("keyword"),
        PROJECT_ORGANIZATION(
            "parent_unit_id",
            "organization",
            PATTERN_IS_URL,
            INVALID_URI_MESSAGE,
            true),
        PROJECT_MANAGER("project_manager"),
        PROJECT_MODIFIED_SINCE(
            "modified_since",
            null,
            PATTERN_IS_DATE),
        PROJECT_PARTICIPANT("participant"),
        PROJECT_UNIT("unit"),
        QUERY(
            "query",
            null,
            PATTERN_IS_NON_EMPTY,
            invalidQueryParametersMessage("query",
                                          ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE),
              true),
        STATUS("status", PATTERN_IS_STATUS, true),
        TITLE(
            "title",
            null,
            PATTERN_IS_TITLE,
            invalidQueryParametersMessage("title",
                                          ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE),
            true),
        USER("user"),
        PAGE_CURRENT(
            "page",
            null,
            PATTERN_IS_NUMBER6,
            ERROR_MESSAGE_INVALID_NUMBER,
            false),
        PAGE_ITEMS_PER_PAGE(
            "per_page",
            "results",
            PATTERN_IS_NUMBER6,
            ERROR_MESSAGE_INVALID_NUMBER,
            false),
        PAGE_SORT("sort");

        public final static int IGNORE_PATH_PARAMETER_INDEX = 3;

        public static final Set<QueryParameterKey> VALID_QUERY_PARAMETERS =
            Arrays.stream(QueryParameterKey.values())
                .filter(f -> f.ordinal() > IGNORE_PATH_PARAMETER_INDEX)
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
            this(cristinKey, null, PATTERN_IS_NON_EMPTY, null, false);
        }

        QueryParameterKey(String cristinKey, String pattern, boolean encode) {
            this(cristinKey, null, pattern, null, encode);
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
                 .filter(f -> f.ordinal() > IGNORE_PATH_PARAMETER_INDEX)
                .filter(equalTo(paramName)).collect(Collectors.toSet());
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
}
