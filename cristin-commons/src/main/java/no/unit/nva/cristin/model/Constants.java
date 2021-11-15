package no.unit.nva.cristin.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import nva.commons.apigateway.MediaTypes;
import nva.commons.core.Environment;
import nva.commons.core.JsonUtils;

import java.util.List;

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
    public static final String CRISTIN_API_HOST = ENVIRONMENT.readEnvOpt("CRISTIN_API_HOST")
            .orElse("api.cristin.no");
    public static final String CRISTIN_API_BASE_URL = "https://" + CRISTIN_API_HOST + "/v2";
    public static final String HTTPS = "https";
    public static final String INSTITUTION_PATH = "institutions";
    public static final String PERSON_PATH = "persons";
    public static final String BASE_PATH = ENVIRONMENT.readEnv("BASE_PATH");
    public static final String DOMAIN_NAME = ENVIRONMENT.readEnvOpt("DOMAIN_NAME")
            .orElse("api.dev.nva.aws.unit.no");
    public static final String FIRST_PAGE = "1";
    public static final String DEFAULT_NUMBER_OF_RESULTS = "5";

    public static final List<MediaType> DEFAULT_RESPONSE_MEDIA_TYPES = List.of(MediaType.JSON_UTF_8,
            MediaTypes.APPLICATION_JSON_LD);
    public static final String ORGANIZATION_PATH = "organization";

    public enum QueryType {
        QUERY_USING_GRANT_ID,
        QUERY_USING_TITLE
    }

}
