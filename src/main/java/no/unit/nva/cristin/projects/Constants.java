package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.databind.ObjectMapper;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonUtils;

@JacocoGenerated
public class Constants {

    public static final ObjectMapper OBJECT_MAPPER = JsonUtils.objectMapper;
    private static final Environment ENVIRONMENT = new Environment();
    private static final String CRISTIN_API_HOST_ENV = "CRISTIN_API_HOST";
    public static final String PROJECT_SEARCH_CONTEXT_URL = "https://example.org/search-api-context.json";
    public static final String PROJECT_LOOKUP_CONTEXT_URL = "https://example.org/project-context.json";
    public static final String CRISTIN_API_HOST = ENVIRONMENT.readEnv(CRISTIN_API_HOST_ENV);
    public static final String CRISTIN_API_BASE_URL = "https://" + CRISTIN_API_HOST + "/v2";
    public static final String HTTPS = "https";
    public static final String EMPTY_FRAGMENT = null;
    public static final String INSTITUTION_PATH = "institutions";
    public static final String PERSON_PATH = "persons";
    private static final String BASE_URL_ENV = "BASE_URL";
    public static final String BASE_URL = ENVIRONMENT.readEnv(BASE_URL_ENV);
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String LANGUAGE = "language";
    public static final String QUESTION_MARK = "?";
    public static final String PROJECTS_PATH = "/project/";
    private static final String DOMAIN_NAME_ENV = "DOMAIN_NAME";
    public static final String DOMAIN_NAME = ENVIRONMENT.readEnv(DOMAIN_NAME_ENV);
    public static final String X_TOTAL_COUNT = "x-total-count";
    public static final String PAGE = "page";
    public static final String FIRST_PAGE = "1";
    public static final String NUMBER_OF_RESULTS = "results";
    public static final String DEFAULT_NUMBER_OF_RESULTS = "5";
    public static final String LINK = "link";
    public static final String REL_NEXT = "rel=\"next\"";
    public static final String REL_PREV = "rel=\"prev\"";
}
