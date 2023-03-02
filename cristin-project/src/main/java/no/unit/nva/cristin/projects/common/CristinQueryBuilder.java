package no.unit.nva.cristin.projects.common;

import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.*;
import static no.unit.nva.cristin.common.handler.CristinHandler.DEFAULT_LANGUAGE_CODE;
import static no.unit.nva.cristin.model.Constants.PATTERN_IS_URL;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.*;
import static no.unit.nva.cristin.projects.common.CristinQuery.getUnitIdFromOrganization;
import static nva.commons.apigateway.RestRequestHandler.EMPTY_STRING;
import static nva.commons.core.attempt.Try.attempt;

/**
 * Builds Cristin query parameters using builder methods and NVA input parameters.
 */
public class CristinQueryBuilder {

    private static final String PARAMETER_PAGE_DEFAULT_VALUE = "1";
    private static final String PARAMETER_PER_PAGE_DEFAULT_VALUE = "5";
    private final transient Set<String> invalidKeys = new HashSet<>(0);
    private final transient CristinQuery cristinQuery;

    /**
     * Constructor of CristinQuery.Builder.
     * <p>Usage:</p>
     * <samp>new CristinQuery.Builder()<br>
     * .fromRequestInfo(requestInfo)<br>
     * .withRequiredParameters(IDENTITY,PAGE_CURRENT,PAGE_ITEMS_PER_PAGE)<br>
     * .validate()<br>.build()
     * </samp>
     */
    public CristinQueryBuilder() {
        cristinQuery = new CristinQuery();
    }

    /**
     * Builder of CristinQuery.
     */
    public CristinQuery build() {
        return cristinQuery;
    }

    /**
     * Validator of CristinQuery.Builder.
     * @throws BadRequestException if parameters are invalid or missing
     */
    public CristinQueryBuilder validate() throws BadRequestException {
        assignDefaultValues();
        for (Map.Entry<QueryParameterKey, String> entry : cristinQuery.pathParameters.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
            throwInvalidPathValue(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<QueryParameterKey, String> entry : cristinQuery.queryParameters.entrySet()) {
            throwInvalidParamererValue(entry.getKey(), entry.getValue());
        }
        if (!requiredMissing().isEmpty()) {
            throw new BadRequestException(requiredMissingMessage(getMissingKeys()));
        }
        if (oneOrMoreOptionalIsMissing()) {
            throw new BadRequestException(validQueryParameterNamesMessage(validKeys()));
        }
        if (!invalidKeys.isEmpty()) {
            throw new BadRequestException(validQueryParameterNamesMessage(validKeys()));
        }
        return this;
    }


    /**
     * Adds query and path parameters from requestInfo.
     */
    public CristinQueryBuilder fromRequestInfo(RequestInfo requestInfo) {
        fromPathParameters(requestInfo.getPathParameters());
        fromQueryParameters(requestInfo.getQueryParameters());
        return this;
    }

    /**
     * Adds parameters from path.
     * */
    public CristinQueryBuilder fromPathParameters(Map<String, String> parameters) {
        parameters.forEach(this::setPath);
        return this;
    }

    /**
     * Adds parameters from query.
     */
    public CristinQueryBuilder fromQueryParameters(Map<String, String> parameters) {
        parameters.forEach(this::setValue);
        return this;
    }

    /**
     * Defines which parameters are required.
     * @param requiredParameters comma seperated QueryParameterKeys
     */
    public CristinQueryBuilder withRequiredParameters(QueryParameterKey... requiredParameters) {
        var tmpSet = Set.of(requiredParameters);
        cristinQuery.isNvaQuery = cristinQuery.isNvaQuery
            || tmpSet.contains(PATH_ORGANISATION)
            || tmpSet.contains(PATH_PROJECT);
        cristinQuery.otherRequiredKeys.addAll(tmpSet);
        return this;
    }

    /**
     * Setter Organization.
     */
    public CristinQueryBuilder asNvaQuery() {
        cristinQuery.isNvaQuery = true;
        return this;
    }

    /**
     * Setter code for the authority that evaluated a project approval.
     */
    public CristinQueryBuilder withApprovedBy(String approvedby) {
        cristinQuery.setValue(PROJECT_APPROVED_BY, approvedby);
        return this;
    }

    /**
     * Setter reference id of project approval.
     */
    public CristinQueryBuilder withApprovalReferenceId(String approvalReferenceId) {
        cristinQuery.setValue(PROJECT_APPROVAL_REFERENCE_ID, approvalReferenceId);
        return this;
    }

    /**
     * Setter Biobank id.
     */
    public CristinQueryBuilder withBiobank(String biobank) {
        cristinQuery.setValue(BIOBANK, biobank);
        return this;
    }

    /**
     * Setter funding source code e.g: NFR, and project_code together separated by ':'
     */
    public CristinQueryBuilder withFunding(String funding) {
        cristinQuery.setValue(FUNDING, funding);
        return this;
    }

    /**
     * Setter funding source code.
     */
    public CristinQueryBuilder withFundingSource(String fundingSource) {
        cristinQuery.setValue(FUNDING_SOURCE, fundingSource);
        return this;
    }


    /**
     * Builder GRANT ID..
     */
    public CristinQueryBuilder withGrantId(String grantId) {
        cristinQuery.setValue(GRANT_ID, grantId);
        return this;
    }

    public CristinQueryBuilder withIdentity(String identifier) {
        return withPathIdentity(identifier);
    }

    /**
     * Setter coordinating institution by cristin id, acronym, name, or part of the name.
     */
    public CristinQueryBuilder withInstitution(String institution) {
        cristinQuery.setValue(INSTITUTION, institution);
        return this;
    }

    /**
     * Start of pagination.
     */
    public CristinQueryBuilder withItemsFromPage(String page) {
        cristinQuery.setValue(PAGE_CURRENT, page);
        return this;
    }

    /**
     * Items per page.
     */
    public CristinQueryBuilder withItemsPerPage(String itemsPerPage) {
        cristinQuery.setValue(PAGE_ITEMS_PER_PAGE, itemsPerPage);
        return this;
    }

    /**
     * Setter sorting on 'start_date' and/or 'end_date'.
     */
    public CristinQueryBuilder withItemSort(String sort) {
        cristinQuery.setValue(PAGE_SORT, sort);
        return this;
    }

    /**
     * Setter search by keyword.
     */
    public CristinQueryBuilder withKeyword(String keyword) {
        cristinQuery.setValue(PROJECT_KEYWORD, keyword);
        return this;
    }

    /**
     * Preferred language.
     */
    public CristinQueryBuilder withLanguage(String language) {
        var langOrDefault = nonNull(language)
            ? language
            : DEFAULT_LANGUAGE_CODE;
        cristinQuery.setValue(LANGUAGE, langOrDefault);
        return this;
    }

    /**
     * Setter how many levels down from 'parent_unit_id' will be included in the search.
     */
    public CristinQueryBuilder withLevels(String levels) {
        cristinQuery.setValue(LEVELS, levels);
        return this;
    }

    /**
     * Setter only those projects that have been modified since this date will be returned.
     */
    public CristinQueryBuilder withModifiedSince(String modifiedSince) {
        cristinQuery.setValue(PROJECT_MODIFIED_SINCE, modifiedSince);
        return this;
    }

    /**
     * Setter Name.
     */
    public CristinQueryBuilder withName(String name) {
        cristinQuery.setValue(NAME, name);
        return this;
    }

    /**
     * Setter Organization.
     */
    public CristinQueryBuilder withOrganization(String organization) {
        if (nonNull(organization)) {
            cristinQuery.setValue(PROJECT_ORGANIZATION, organization);
        } else {
            cristinQuery.setValue(QUERY, EMPTY_STRING);
        }
        return this;
    }

    /**
     * Setter a participant of the project by Cristin id, name or part of the name.
     */
    public CristinQueryBuilder withParentUnitId(String parentUnitId) {
        if (nonNull(parentUnitId)) {
            var unitId = parentUnitId;
            if (parentUnitId.matches(PATTERN_IS_URL)) {
                unitId = getUnitIdFromOrganization(parentUnitId);
            }
            cristinQuery.setPath(PATH_ORGANISATION, unitId);
        }
        return this;
    }

    /**
     * Setter a participant of the project by Cristin id, name or part of the name.
     */
    public CristinQueryBuilder withParticipant(String participant) {
        cristinQuery.setValue(PROJECT_PARTICIPANT, participant);
        return this;
    }

    /**
     * Setter Identity.
     */
    public CristinQueryBuilder withPathIdentity(String identity) {
        System.out.println("withPathIdentity " + identity);
        if (nonNull(identity) && !identity.isBlank()) {
            if (identity.matches(PATH_PROJECT.getPattern())) {
                cristinQuery.setPath(PATH_PROJECT, identity);
            } else if (identity.matches(PATH_ORGANISATION.getPattern())) {
                cristinQuery.setPath(PATH_ORGANISATION, identity);
            }
        } else {
            cristinQuery.setValue(PATH_PROJECT, EMPTY_STRING);
        }
        return this;
    }

    /**
     * Setter Organization.
     */
    public CristinQueryBuilder withPathOrganization(String organization) {
        System.out.println("withPathOrganization " + organization);
        if (nonNull(organization)) {
            if (organization.matches(PROJECT_ORGANIZATION.getPattern())) {
                cristinQuery.setValue(PROJECT_ORGANIZATION,organization);
            } else {
                cristinQuery.setPath(PATH_ORGANISATION, organization);
            }
        } else {
            cristinQuery.setValue(PATH_ORGANISATION, EMPTY_STRING);
        }
        return this;
    }

    /**
     * Setter project manager by Cristin id, name or part of the name.
     */
    public CristinQueryBuilder withProjectManager(String projectManager) {
        cristinQuery.setValue(PROJECT_MANAGER, projectManager);
        return this;
    }

    /**
     * Setter Query.
     */
    public CristinQueryBuilder withQuery(String query) {
        if (nonNull(query)) {
            var key = grantOrTitleKey(query);
            cristinQuery.setValue(key, query);
        } else {
            cristinQuery.setValue(QUERY, EMPTY_STRING);
        }
        return this;
    }

    /**
     * Setter status of projects.
     */
    public CristinQueryBuilder withStatus(String status) {
        var statusKind = attempt(() ->
            ProjectStatus.valueOf(status.toUpperCase(Locale.getDefault())).name())
            .orElse((e) -> EMPTY_STRING);
        cristinQuery.setValue(STATUS, statusKind);
        return this;
    }

    /**
     * Setter Biobank id.
     */
    public CristinQueryBuilder withTitle(String title) {
        cristinQuery.setValue(TITLE, title);
        return this;
    }

    /**
     * Setter unit id.
     */
    public CristinQueryBuilder withUnit(String unit) {
        cristinQuery.setValue(PROJECT_UNIT, unit);
        return this;
    }

    /**
     * Setter a person's username in Cristin together with the institution id separated by ':'.
     */
    public CristinQueryBuilder withUser(String user) {
        cristinQuery.setValue(USER, user);
        return this;
    }

    /**
     * End Setters * Start private functions.
     */

    private void assignDefaultValues() {
        requiredMissing().forEach(key -> {
            switch (key) {
                case LANGUAGE:
                    cristinQuery.setValue(key, DEFAULT_LANGUAGE_CODE);
                    break;
                case PAGE_CURRENT:
                    cristinQuery.setValue(key, PARAMETER_PAGE_DEFAULT_VALUE);
                    break;
                case PAGE_ITEMS_PER_PAGE:
                    cristinQuery.setValue(key, PARAMETER_PER_PAGE_DEFAULT_VALUE);
                    break;
                default:
                    break;
            }
        });
    }

    private void setPath(String key, String value) {
        cristinQuery.isNvaQuery = true;
        var nonNullValue = nonNull(value) ? value : EMPTY_STRING;
        if (key.equals(IDENTITY.getNvaKey()) || key.equals(IDENTITY.getKey())
            || key.equals(PATH_PROJECT.getNvaKey()) || key.equals(PATH_PROJECT.getKey())) {
            withPathIdentity(nonNullValue);
        } else if (key.equals(PATH_ORGANISATION.getNvaKey())) {
            withPathOrganization(nonNullValue);
        } else {
            invalidKeys.add(key);
        }
    }

    private void setValue(String key, String value) {
        var qpKey = keyFromString(key,value);
        if (!key.equals(qpKey.getKey()) && !cristinQuery.isNvaQuery && qpKey != INVALID) {
            cristinQuery.isNvaQuery = true;
        }
        switch (qpKey) {
            case PATH_ORGANISATION:
                withPathOrganization(value);
                break;
            case PATH_PROJECT:
            case IDENTITY:
                withPathIdentity(value);
                break;
            case PROJECT_ORGANIZATION:
                withOrganization(value);
                break;
            case BIOBANK:
            case FUNDING:
            case FUNDING_SOURCE:
            case GRANT_ID:
            case INSTITUTION:
            case LEVELS:
            case NAME:
            case PAGE_CURRENT:
            case PAGE_ITEMS_PER_PAGE:
            case PAGE_SORT:
            case PROJECT_APPROVAL_REFERENCE_ID:
            case PROJECT_APPROVED_BY:
            case PROJECT_KEYWORD:
            case PROJECT_MANAGER:
            case PROJECT_MODIFIED_SINCE:
            case PROJECT_PARTICIPANT:
            case PROJECT_UNIT:
            case USER:
                cristinQuery.setValue(qpKey, value);
                break;
            case TITLE:
                withTitle(value);
                break;
            case LANGUAGE:
                withLanguage(value);
                break;
            case QUERY:
                withQuery(value);
                break;
            case STATUS:
                withStatus(value);
                break;
            default:
                invalidKeys.add(key);
                break;
        }
    }

    private boolean oneOrMoreOptionalIsMissing() {
        return oneOrMoreOptionalUnassigned().size() == oneOrMoreOptional().size() && !requiredMissing().isEmpty();
    }

    private boolean invalidQueryParameter(QueryParameterKey key, String value) {
        return isNull(value) || !value.matches(key.getPattern());
    }

    private Set<String> getMissingKeys() {
        return
            requiredMissing()
                .stream()
                .map(qpk -> cristinQuery.isNvaQuery ? qpk.getNvaKey() : qpk.getKey())
                .collect(Collectors.toSet());
    }



    private QueryParameterKey grantOrTitleKey(String query) {
        return Utils.isPositiveInteger(query) ? GRANT_ID : TITLE;
    }

    private Set<QueryParameterKey> oneOrMoreOptional() {
        return
            VALID_QUERY_PARAMETERS.stream()
                .filter(key -> !required().contains(key))
                .collect(Collectors.toSet());
    }

    private Set<QueryParameterKey> oneOrMoreOptionalUnassigned() {
        return VALID_QUERY_PARAMETERS.stream()
            .filter(key -> !cristinQuery.containsKey(key)
                && !required().contains(key))
            .collect(Collectors.toSet());
    }

    private Set<QueryParameterKey> required() {
        return
            Stream.concat(
                    cristinQuery.otherRequiredKeys.stream(),
                    cristinQuery.pathParameters.keySet().stream())
                .collect(Collectors.toSet());
    }

    private Set<QueryParameterKey> requiredMissing() {
        return
            required().stream()
                .filter(key -> !cristinQuery.queryParameters.containsKey(key))
                .filter(key -> !cristinQuery.pathParameters.containsKey(key))
                .collect(Collectors.toSet());
    }

    private Set<String> validKeys() {
        return cristinQuery.isNvaQuery ? VALID_QUERY_PARAMETER_NVA_KEYS : VALID_QUERY_PARAMETER_KEYS;
    }

    private void throwInvalidParamererValue(QueryParameterKey key, String value) throws BadRequestException {
        if (invalidQueryParameter(key, value)) {
            final var keyName = cristinQuery.isNvaQuery ? key.getNvaKey() : key.getKey();
            String errorMessage;
            if (key == STATUS) {
                errorMessage =
                    invalidQueryParametersMessageWithRange(key.getKey(), Arrays.toString(ProjectStatus.values()));
            } else if (nonNull(key.getErrorMessage())) {
                errorMessage = String.format(key.getErrorMessage(), keyName);
            } else {
                errorMessage = invalidQueryParametersMessage(keyName, EMPTY_STRING);
            }
            throw new BadRequestException(errorMessage);
        }
    }

    private void throwInvalidPathValue(QueryParameterKey key, String value) throws BadRequestException {
        if (invalidQueryParameter(key, value)) {
            final var keyName = cristinQuery.isNvaQuery ? key.getNvaKey() : key.getKey();
            final var errorMessage = nonNull(key.getErrorMessage())
                ? key.getErrorMessage()
                : invalidPathParameterMessage(keyName);
            throw new BadRequestException(errorMessage);
        }
    }
}