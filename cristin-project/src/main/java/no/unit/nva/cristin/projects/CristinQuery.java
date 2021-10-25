package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.common.util.UriUtils.queryParameters;
import static no.unit.nva.cristin.projects.Constants.CRISTIN_API_HOST;
import static no.unit.nva.cristin.projects.Constants.EMPTY_FRAGMENT;
import static no.unit.nva.cristin.projects.Constants.HTTPS;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CristinQuery {

    private static final String CRISTIN_QUERY_PARAMETER_PROJECT_CODE_KEY = "project_code";
    private static final String CRISTIN_QUERY_PARAMETER_TITLE_KEY = "title";
    private static final String CRISTIN_QUERY_PARAMETER_LANGUAGE_KEY = "lang";
    private static final String CRISTIN_QUERY_PARAMETER_PAGE_KEY = "page";
    private static final String CRISTIN_QUERY_PARAMETER_PAGE_DEFAULT_VALUE = "1";
    private static final String CRISTIN_QUERY_PARAMETER_PER_PAGE_KEY = "per_page";
    private static final String CRISTIN_QUERY_PARAMETER_PER_PAGE_DEFAULT_VALUE = "5";
    private static final String CRISTIN_API_PROJECTS_PATH = "/v2/projects/";

    private final transient Map<String, String> cristinQueryParameters;

    /**
     * Creates a object used to generate URI to connect to Cristin Projects.
     */
    public CristinQuery() {
        cristinQueryParameters = new ConcurrentHashMap<>();
        cristinQueryParameters.put(
            CRISTIN_QUERY_PARAMETER_PAGE_KEY,
            CRISTIN_QUERY_PARAMETER_PAGE_DEFAULT_VALUE);
        cristinQueryParameters.put(
            CRISTIN_QUERY_PARAMETER_PER_PAGE_KEY,
            CRISTIN_QUERY_PARAMETER_PER_PAGE_DEFAULT_VALUE);
    }

    /**
     * Creates a URI to Cristin project with specific ID and language.
     *
     * @param id       Project ID to lookup in Cristin
     * @param language what language we want some of the result fields to be in
     * @return an URI to Cristin Projects with ID and language parameters
     * @throws URISyntaxException if URI is invalid
     */
    public static URI fromIdAndLanguage(String id, String language) throws URISyntaxException {
        return new URI(
            HTTPS,
            CRISTIN_API_HOST,
            CRISTIN_API_PROJECTS_PATH + id,
            queryParameters(Map.of(CRISTIN_QUERY_PARAMETER_LANGUAGE_KEY, language)),
            EMPTY_FRAGMENT);
    }

    public CristinQuery withGrantId(String grantId) {
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_PROJECT_CODE_KEY, grantId);
        return this;
    }

    public CristinQuery withTitle(String title) {
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_TITLE_KEY, title);
        return this;
    }

    public CristinQuery withLanguage(String language) {
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_LANGUAGE_KEY, language);
        return this;
    }

    public CristinQuery withFromPage(String page) {
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_PAGE_KEY, page);
        return this;
    }

    public CristinQuery withItemsPerPage(String itemsPerPage) {
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_PER_PAGE_KEY, itemsPerPage);
        return this;
    }

    /**
     * Builds URI to search Cristin projects based on parameters supplied to the builder methods.
     *
     * @return an URI to Cristin Projects with parameters
     * @throws URISyntaxException if URI is invalid
     */
    public URI toURI() throws URISyntaxException {
        return new URI(
            HTTPS,
            CRISTIN_API_HOST,
            CRISTIN_API_PROJECTS_PATH,
            queryParameters(cristinQueryParameters),
            EMPTY_FRAGMENT);
    }
}
