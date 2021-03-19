package no.unit.nva.cristin.projects;

import nva.commons.core.Environment;

public class Constants {

    private static final Environment ENVIRONMENT = new Environment();
    private static final String CRISTIN_API_HOST_ENV = "CRISTIN_API_HOST";
    public static final String PROJECT_SEARCH_CONTEXT_URL = "https://example.org/search-api-context.json";
    public static final String PROJECT_LOOKUP_CONTEXT_URL = "https://example.org/project-context.json";
    public static final String CRISTIN_API_HOST = ENVIRONMENT.readEnv(CRISTIN_API_HOST_ENV);
    // TODO: Improve this in NP-2281?
    public static final String CRISTIN_API_BASE_URL = "https://" + CRISTIN_API_HOST + "/v2";
    public static final String INSTITUTION_PATH = "institutions";
    public static final String PERSON_PATH = "persons";
    private static final String BASE_URL_ENV = "BASE_URL";
    public static final String BASE_URL = ENVIRONMENT.readEnv(BASE_URL_ENV);
}
