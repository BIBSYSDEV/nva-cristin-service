package no.unit.nva.cristin.projects;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.STATUS;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;

public class CristinQuery {

    private static final String CRISTIN_QUERY_PARAMETER_PROJECT_CODE_KEY = "project_code";
    private static final String CRISTIN_QUERY_PARAMETER_TITLE_KEY = "title";
    private static final String CRISTIN_QUERY_PARAMETER_LANGUAGE_KEY = "lang";
    private static final String CRISTIN_QUERY_PARAMETER_PAGE_KEY = "page";
    private static final String CRISTIN_QUERY_PARAMETER_PAGE_DEFAULT_VALUE = "1";
    private static final String CRISTIN_QUERY_PARAMETER_PER_PAGE_KEY = "per_page";
    public static final String CRISTIN_QUERY_PARAMETER_PARENT_UNIT_ID = "parent_unit_id";
    private static final String CRISTIN_QUERY_PARAMETER_PER_PAGE_DEFAULT_VALUE = "5";
    private static final String CRISTIN_API_PROJECTS_PATH = "projects";
    private static final String CRISTIN_QUERY_PARAMETER_STATUS = "status";



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
     */
    public static URI fromIdAndLanguage(String id, String language) {
        return UriWrapper.fromUri(CRISTIN_API_URL)
                .addChild(CRISTIN_API_PROJECTS_PATH)
                .addChild(id)
                .addQueryParameters(Map.of(CRISTIN_QUERY_PARAMETER_LANGUAGE_KEY, language))
                .getUri();
    }

    public CristinQuery withGrantId(String grantId) {
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_PROJECT_CODE_KEY, grantId);
        return this;
    }

    public CristinQuery withTitle(String title) {
        cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_TITLE_KEY, title);
        return this;
    }

    /**
     * Preferred language.
     */
    public CristinQuery withLanguage(String language) {
        if (nonNull(language)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_LANGUAGE_KEY, language);
        }
        return this;
    }

    /**
     * Start of pagination.
     */
    public CristinQuery withFromPage(String page) {
        if (nonNull(page)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_PAGE_KEY, page);
        }
        return this;
    }

    /**
     * Items per page.
     */
    public CristinQuery withItemsPerPage(String itemsPerPage) {
        if (nonNull(itemsPerPage)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_PER_PAGE_KEY, itemsPerPage);
        }
        return this;
    }

    /**
     * Organization to start searching from. Includes sublevels if requested.
     */
    public CristinQuery withParentUnitId(String parentUnitId) {
        if (nonNull(parentUnitId)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_PARENT_UNIT_ID, getUnitIdFromOrganization(parentUnitId));
        }
        return this;
    }

    private String getUnitIdFromOrganization(String organizationId) {
        return extractLastPathElement(URI.create(organizationId));
    }

    /**
     * Requested status of projects.
     */
    public CristinQuery withStatus(String status) {
        if (nonNull(status)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_STATUS, getEncodedStatusParameter(status));
        }
        return this;
    }

    private String getEncodedStatusParameter(String status) {
        return URLEncoder.encode(ProjectStatus.getNvaStatus(status).getCristinStatus(), StandardCharsets.UTF_8);
    }


    /**
     * Builds URI to search Cristin projects based on parameters supplied to the builder methods.
     *
     * @return an URI to Cristin Projects with parameters
     */
    public URI toURI() {
        return UriWrapper.fromUri(CRISTIN_API_URL)
                .addChild(CRISTIN_API_PROJECTS_PATH)
                .addQueryParameters(cristinQueryParameters)
                .getUri();

    }

    /**
     * Builds Cristin query parameters using builder methods and NVA input parameters.
     */
    public CristinQuery generateQueryParameters(Map<String, String> parameters) {
        withLanguage(parameters.get(LANGUAGE));
        withFromPage(parameters.get(PAGE));
        withItemsPerPage(parameters.get(NUMBER_OF_RESULTS));
        withParentUnitId(parameters.get(ORGANIZATION));
        withStatus(parameters.get(STATUS));

        return this;
    }
}
