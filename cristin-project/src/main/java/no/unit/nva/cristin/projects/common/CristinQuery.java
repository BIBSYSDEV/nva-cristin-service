package no.unit.nva.cristin.projects.common;

import no.unit.nva.cristin.model.Constants;
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.JsonPropertyNames.BIOBANK_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_INSTITUTION_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.FUNDING;
import static no.unit.nva.cristin.model.JsonPropertyNames.FUNDING_SOURCE;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.LEVELS;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_APPROVAL_REFERENCE_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_APPROVED_BY;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_KEYWORD;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_MANAGER;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_MODIFIED_SINCE;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_PARTICIPANT;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_SORT;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_UNIT;
import static no.unit.nva.cristin.model.JsonPropertyNames.STATUS;
import static no.unit.nva.cristin.model.JsonPropertyNames.USER;
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
    private static final String CRISTIN_QUERY_PARAMETER_INSTITUTION = "institution";
    private static final String CRISTIN_QUERY_PARAMETER_PROJECT_MANAGER = "project_manager";
    private static final String CRISTIN_QUERY_PARAMETER_PARTICIPANT = "participant";
    private static final String CRISTIN_QUERY_PARAMETER_KEYWORD = "keyword";
    private static final String CRISTIN_QUERY_PARAMETER_FUNDING_SOURCE = "funding_source";
    private static final String CRISTIN_QUERY_PARAMETER_APPROVAL_REFERENCE_ID = "approval_reference_id";
    private static final String CRISTIN_QUERY_PARAMETER_SORT = "sort";
    private static final String CRISTIN_QUERY_PARAMETER_UNIT = "unit";
    private static final String CRISTIN_QUERY_PARAMETER_USER = "user";
    private static final String CRISTIN_QUERY_PARAMETER_APPROVED_BY = "approved_by";
    private static final String CRISTIN_QUERY_PARAMETER_FUNDING = "funding";
    private static final String CRISTIN_QUERY_PARAMETER_LEVELS = "levels";
    private static final String CRISTIN_QUERY_PARAMETER_MODIFIED_SINCE = "modified_since";
    private static final String CRISTIN_QUERY_PARAMETER_BIOBANK = "biobank";



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
        return UriWrapper.fromUri(Constants.CRISTIN_API_URL)
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
     * Requested coordinating institution by cristin id, acronym, name, or part of the name.
     */

    public CristinQuery withInstitution(String institution) {
        if (nonNull(institution)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_INSTITUTION, institution);
        }
        return this;
    }

    /**
     * Requested project manager by Cristin id, name or part of the name.
     */
    public CristinQuery withProjectManager(String projectManager) {
        if (nonNull(projectManager)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_PROJECT_MANAGER, projectManager);
        }
        return this;
    }

    /**
     * Requested a participant of the project by Cristin id, name or part of the name.
     */
    public CristinQuery withParticipant(String participant) {
        if (nonNull(participant)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_PARTICIPANT, participant);
        }
        return this;
    }

    /**
     * Requested search by keyword.
     */
    public CristinQuery withKeyword(String keyword) {
        if (nonNull(keyword)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_KEYWORD, keyword);
        }
        return this;
    }

    /**
     * Requested funding source code.
     */
    public CristinQuery withFundingSource(String fundingSource) {
        if (nonNull(fundingSource)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_FUNDING_SOURCE, fundingSource);
        }
        return this;
    }

    /**
     * Requested reference id of project approval.
     */
    public CristinQuery withApprovalReferenceId(String approvalReferenceId) {
        if (nonNull(approvalReferenceId)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_APPROVAL_REFERENCE_ID, approvalReferenceId);
        }
        return this;
    }

    /**
     * Requested sorting on 'start_date' and/or 'end_date'.
     */
    public CristinQuery withSort(String sort) {
        if (nonNull(sort)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_SORT, sort);
        }
        return this;
    }

    /**
     * Requested unit id.
     */
    public CristinQuery withUnit(String unit) {
        if (nonNull(unit)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_UNIT, unit);
        }
        return this;
    }

    /**
     * Requested a person's username in Cristin together with the institution id separated by ':'.
     */
    public CristinQuery withUser(String user) {
        if (nonNull(user)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_USER, user);
        }
        return this;
    }

    /**
     * Requested code for the authority that evaluated a project approval.
     */
    public CristinQuery withApprovedBy(String approvedby) {
        if (nonNull(approvedby)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_APPROVED_BY, approvedby);
        }
        return this;
    }

    /**
     * Requested funding source code e.g: NFR, and project_code together separated by ':'
     */
    public CristinQuery withFunding(String funding) {
        if (nonNull(funding)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_FUNDING, funding);
        }
        return this;
    }

    /**
     * Requested how many levels down from 'parent_unit_id' will be included in the search.
     */
    public CristinQuery withLevels(String levels) {
        if (nonNull(levels)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_LEVELS, levels);
        }
        return this;
    }

    /**
     * Requested only those projects that have been modified since this date will be returned.
     */
    public CristinQuery withModifiedSince(String modifiedSince) {
        if (nonNull(modifiedSince)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_MODIFIED_SINCE, modifiedSince);
        }
        return this;
    }

    /**
     * Requested Biobank id.
     */
    public CristinQuery withBiobank(String biobank) {
        if (nonNull(biobank)) {
            cristinQueryParameters.put(CRISTIN_QUERY_PARAMETER_BIOBANK, biobank);
        }
        return this;
    }

    /**
     * Builds URI to search Cristin projects based on parameters supplied to the builder methods.
     *
     * @return an URI to Cristin Projects with parameters
     */
    public URI toURI() {
        return UriWrapper.fromUri(Constants.CRISTIN_API_URL)
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
        withInstitution(parameters.get(CRISTIN_INSTITUTION_ID));
        withProjectManager(parameters.get(PROJECT_MANAGER));
        withParticipant(parameters.get(PROJECT_PARTICIPANT));
        withKeyword(parameters.get(PROJECT_KEYWORD));
        withFundingSource(parameters.get(FUNDING_SOURCE));
        withFunding(parameters.get(FUNDING));
        withApprovalReferenceId(parameters.get(PROJECT_APPROVAL_REFERENCE_ID));
        withApprovedBy(parameters.get(PROJECT_APPROVED_BY));
        withSort(parameters.get(PROJECT_SORT));
        withModifiedSince(PROJECT_MODIFIED_SINCE);
        withUnit(parameters.get(PROJECT_UNIT));
        withUser(parameters.get(USER));
        withLevels(parameters.get(LEVELS));
        withBiobank(parameters.get(BIOBANK_ID));


        return this;
    }
}
