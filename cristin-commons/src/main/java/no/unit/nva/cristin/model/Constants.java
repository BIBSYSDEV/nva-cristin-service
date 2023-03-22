package no.unit.nva.cristin.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import java.util.List;
import java.util.regex.Pattern;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.apigateway.MediaTypes;
import nva.commons.core.Environment;

import static no.unit.nva.cristin.common.Utils.forceUTF8;

public class Constants {

    public static final Environment ENVIRONMENT = new Environment();
    public static final List<MediaType> DEFAULT_RESPONSE_MEDIA_TYPES =
        List.of(MediaType.JSON_UTF_8,MediaTypes.APPLICATION_JSON_LD);

    public static final ObjectMapper OBJECT_MAPPER = JsonUtils.dtoObjectMapper;
    public static final Pattern ORCID_PATTERN = Pattern.compile("[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{3}[0-9|Xx]");
    public static final String ALL_QUERY_PARAMETER_LANGUAGES = "en,nb,nn";
    public static final String BASE_PATH = ENVIRONMENT.readEnv("BASE_PATH");
    public static final String CRISTIN_API_URL = ENVIRONMENT.readEnv("CRISTIN_API_URL");
    public static final String CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME =
        ENVIRONMENT.readEnv("CRISTIN_BOT_FILTER_BYPASS_HEADER_NAME");

    public static final String CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE =
        ENVIRONMENT.readEnv("CRISTIN_BOT_FILTER_BYPASS_HEADER_VALUE");

    public static final String CRISTIN_INSTITUTION_HEADER = "Cristin-Representing-Institution";
    public static final String CRISTIN_QUERY_NAME_PARAM = "name";
    public static final String CRISTIN_PER_PAGE_PARAM = "per_page";
    public static final String DEFAULT_NUMBER_OF_RESULTS = "5";
    public static final String DOMAIN_NAME =
        ENVIRONMENT.readEnvOpt("DOMAIN_NAME").orElse("api.dev.nva.aws.unit.no");

    public static final String EMPLOYMENT_ID = "employmentId";
    public static final String EQUAL_OPERATOR = "=";
    public static final String FIRST_PAGE = "1";
    public static final String FULL = "full";
    public static final String HASHTAG = "#";
    public static final String HTTPS = "https";
    public static final String INSTITUTION_PATH = "institutions";
    public static final String LINK = "link";
    public static final String NONE = "none";
    public static final String NOT_FOUND_MESSAGE_TEMPLATE = "The resource '%s' cannot be dereferenced";
    public static final String ORGANIZATION_PATH = "organization";
    public static final String ORG_ID = "orgId";
    public static final String PARENT_UNIT_ID = "parent_unit_id";
    public static final String PATTERN_IS_DATE = "(\\d){4}-(\\d){2}-(\\d){2}[T]*[(\\d){2}:(\\d){2}:(\\d){2,6}Z]*";
    public static final String PATTERN_IS_LANGUAGE = "(en|nb|nn|\\,)+";
    public static final String PATTERN_IS_NON_EMPTY = ".+";
    public static final String PATTERN_IS_NUMBER = "[1-9]\\d*";
    public static final String PATTERN_IS_STATUS = "(?i)CONCLUDED|ACTIVE|NOT[ +]*STARTED";
    public static final String PATTERN_IS_TITLE = forceUTF8("^[æøåÆØÅ\\w-,\\.: ]+$");
    public static final String PATTERN_IS_URL =
        "(http(s):\\/\\/.)[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";

    public static final String PERSONS_PATH = "persons";
    public static final String PERSON_CONTEXT = "https://example.org/person-context.json";
    public static final String PERSON_ID = "id";
    public static final String PERSON_PATH = "persons";
    public static final String PERSON_PATH_NVA = "person";
    public static final String PERSON_QUERY_CONTEXT = "https://example.org/person-search-context.json";
    public static final String POSITION = "position";
    public static final String PROJECTS_PATH = "projects";
    public static final String PROJECT_LOOKUP_CONTEXT_URL = "https://bibsysdev.github.io/src/project-context.json";
    public static final String PROJECT_PATH_NVA = "project";
    public static final String PROJECT_SEARCH_CONTEXT_URL =
        "https://bibsysdev.github.io/src/project-search-context.json";

    public static final String QUERY_PARAMETER_LANGUAGE = "lang";
    public static final String REL_NEXT = "rel=\"next\"";
    public static final String REL_PREV = "rel=\"prev\"";
    public static final String SLASH_DELIMITER = "/";
    public static final String SORT = "sort";
    public static final String TOP = "top";
    public static final String UNITS_PATH = "units";
    public static final String UNIT_ID = "id";
    public static final String X_TOTAL_COUNT = "x-total-count";
}
