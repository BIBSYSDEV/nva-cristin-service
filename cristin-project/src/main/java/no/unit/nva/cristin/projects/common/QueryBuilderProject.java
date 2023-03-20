package no.unit.nva.cristin.projects.common;

import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.model.QueryBuilder;
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
import nva.commons.apigateway.exceptions.BadRequestException;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessage;
import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessageWithRange;
import static no.unit.nva.cristin.common.handler.CristinHandler.DEFAULT_LANGUAGE_CODE;
import static no.unit.nva.cristin.model.Constants.PATTERN_IS_URL;
import static no.unit.nva.cristin.model.CristinQuery.getUnitIdFromOrganization;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.APPROVAL_REFERENCE_ID;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.APPROVED_BY;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.BIOBANK;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.FUNDING;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.FUNDING_SOURCE;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.GRANT_ID;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.INSTITUTION;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.INVALID;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.KEYWORD;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.LANGUAGE;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.LEVELS;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.MODIFIED_SINCE;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.NAME;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.ORGANIZATION;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PAGE_CURRENT;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PAGE_ITEMS_PER_PAGE;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PAGE_SORT;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PARTICIPANT;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PATH_IDENTITY;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PATH_ORGANISATION;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PATH_PROJECT;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PROJECT_MANAGER;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PROJECT_UNIT;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.QUERY;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.STATUS;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.TITLE;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.USER;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.VALID_QUERY_PARAMETER_KEYS;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.VALID_QUERY_PARAMETER_NVA_KEYS;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.keyFromString;
import static nva.commons.apigateway.RestRequestHandler.EMPTY_STRING;
import static nva.commons.core.attempt.Try.attempt;

public class QueryBuilderProject extends QueryBuilder<ParameterKeyProject> {

    public QueryBuilderProject() {
        super(new QueryProject());
    }

    @Override
    protected void assignDefaultValues() {
        requiredMissing().forEach(key -> {
            switch (key) {
                case PATH_ORGANISATION:
                    if (query.containsKey(PATH_IDENTITY)) {
                        query.setPath(key, query.getValue(PATH_IDENTITY));
                        query.removeValue(PATH_IDENTITY);
                    }
                    break;
                case PATH_PROJECT:
                    query.setPath(key, EMPTY_STRING);
                    break;
                case LANGUAGE:
                    query.setValue(key, DEFAULT_LANGUAGE_CODE);
                    break;
                case PAGE_CURRENT:
                    query.setValue(key, PARAMETER_PAGE_DEFAULT_VALUE);
                    break;
                case PAGE_ITEMS_PER_PAGE:
                    query.setValue(key, PARAMETER_PER_PAGE_DEFAULT_VALUE);
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    protected void setPath(String key, String value) {
        asNvaQuery();
        var nonNullValue = nonNull(value) ? value : EMPTY_STRING;

        if (key.equals(PATH_IDENTITY.getNvaKey())) {
            withPathIdentity(nonNullValue);
        } else if (key.equals(PATH_ORGANISATION.getNvaKey())) {
            withPathOrganization(nonNullValue);
        } else if (key.equals(PATH_PROJECT.getNvaKey()) || key.equals(PATH_PROJECT.getKey())) {
            withPathProject(nonNullValue);
        } else {
            invalidKeys.add(key);
        }
    }

    @Override
    protected void setValue(String key, String value) {
        var qpKey = keyFromString(key,value);
        if (!key.equals(qpKey.getKey()) && !query.isNvaQuery() && qpKey != INVALID) {
            asNvaQuery();
        }
        switch (qpKey) {
            case PATH_IDENTITY:
            case PATH_PROJECT:
                withPathIdentity(value);
                break;
            case PATH_ORGANISATION:
                withPathOrganization(value);
                break;
            case ORGANIZATION:
                withOrganization(value);
                break;
            case BIOBANK:
                withBiobank(value);
                break;
            case KEYWORD:
                withKeyword(value);
                break;
            case PARTICIPANT:
                withParticipant(value);
                break;
            case QUERY:
                withQuery(value);
                break;
            case STATUS:
                withStatus(value);
                break;
            case APPROVAL_REFERENCE_ID:
            case APPROVED_BY:
            case FUNDING:
            case FUNDING_SOURCE:
            case GRANT_ID:
            case INSTITUTION:
            case LANGUAGE:
            case LEVELS:
            case MODIFIED_SINCE:
            case NAME:
            case PROJECT_MANAGER:
            case PROJECT_UNIT:
            case TITLE:
            case USER:
            case PAGE_CURRENT:
            case PAGE_ITEMS_PER_PAGE:
            case PAGE_SORT:
                query.setValue(qpKey, value);
                break;
            default:
                invalidKeys.add(key);
                break;
        }
    }

    @Override
    protected Set<String> validKeys() {
        return query.isNvaQuery() ? VALID_QUERY_PARAMETER_NVA_KEYS : VALID_QUERY_PARAMETER_KEYS;
    }

    @Override
    protected void throwInvalidParamererValue(Entry<ParameterKeyProject, String> entry) throws BadRequestException {
        final var key = entry.getKey();
        if (invalidQueryParameter(key, entry.getValue())) {
            final var keyName = query.isNvaQuery() ? key.getNvaKey() : key.getKey();
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


    /**
     * Setter code for the authority that evaluated a project approval.
     */
    public QueryBuilderProject withApprovedBy(String approvedby) {
        query.setValue(APPROVED_BY, approvedby);
        return this;
    }

    /**
     * Setter reference id of project approval.
     */
    public QueryBuilderProject withApprovalReferenceId(String approvalReferenceId) {
        query.setValue(APPROVAL_REFERENCE_ID, approvalReferenceId);
        return this;
    }

    /**
     * Setter Biobank id. [withDuplicates]
     */
    public QueryBuilderProject withBiobank(String biobank) {
        var biobanks =
            query.containsKey(BIOBANK)
                ? query.getValue(BIOBANK) + "," + biobank
                : biobank;
        query.setValue(BIOBANK, biobanks);
        return this;
    }

    /**
     * Setter funding source code e.g: NFR, and project_code together separated by ':'
     */
    public QueryBuilderProject withFunding(String funding) {
        query.setValue(FUNDING, funding);
        return this;
    }

    /**
     * Setter funding source code.
     */
    public QueryBuilderProject withFundingSource(String fundingSource) {
        query.setValue(FUNDING_SOURCE, fundingSource);
        return this;
    }

    /**
     * Builder GRANT ID..
     */
    public QueryBuilderProject withGrantId(String grantId) {
        query.setValue(GRANT_ID, grantId);
        return this;
    }

    /**
     * Setter coordinating institution by cristin id, acronym, name, or part of the name.
     */
    public QueryBuilderProject withInstitution(String institution) {
        query.setValue(INSTITUTION, institution);
        return this;
    }

    /**
     * Start of pagination.
     */
    public QueryBuilderProject withItemsFromPage(String page) {
        query.setValue(PAGE_CURRENT, page);
        return this;
    }

    /**
     * Items per page.
     */
    public QueryBuilderProject withItemsPerPage(String itemsPerPage) {
        query.setValue(PAGE_ITEMS_PER_PAGE, itemsPerPage);
        return this;
    }

    /**
     * Setter sorting on 'start_date' and/or 'end_date'.
     */
    public QueryBuilderProject withItemSort(String sort) {
        query.setValue(PAGE_SORT, sort);
        return this;
    }

    /**
     * Setter search by keyword. [withDuplicates]
     */
    public QueryBuilderProject withKeyword(String keyword) {
        var keywords =
            query.containsKey(KEYWORD)
                ? query.getValue(KEYWORD) + "," + keyword
                : keyword;
        query.setValue(KEYWORD, keywords);
        return this;
    }

    /**
     * Preferred language.
     */
    public QueryBuilderProject withLanguage(String language) {
        query.setValue(LANGUAGE, language);
        return this;
    }

    /**
     * Setter how many levels down from 'parent_unit_id' will be included in the search.
     */
    public QueryBuilderProject withLevels(String levels) {
        query.setValue(LEVELS, levels);
        return this;
    }

    /**
     * Setter only those projects that have been modified since this date will be returned.
     */
    public QueryBuilderProject withModifiedSince(String modifiedSince) {
        query.setValue(MODIFIED_SINCE, modifiedSince);
        return this;
    }

    /**
     * Setter Name.
     */
    public QueryBuilderProject withName(String name) {
        query.setValue(NAME, name);
        return this;
    }

    /**
     * Setter Organization.
     */
    public QueryBuilderProject withOrganization(String organization) {
        if (nonNull(organization)) {
            query.setValue(ORGANIZATION, organization);
        } else {
            // this will trigger correct errormessage
            query.setValue(QUERY, EMPTY_STRING);
        }
        return this;
    }

    /**
     * Setter a participant of the project by Cristin id, name or part of the name.
     */
    public QueryBuilderProject withParentUnitId(String parentUnitId) {
        if (nonNull(parentUnitId)) {
            var unitId = parentUnitId;
            if (parentUnitId.matches(PATTERN_IS_URL)) {
                unitId = getUnitIdFromOrganization(parentUnitId);
            }
            query.setPath(PATH_ORGANISATION, unitId);
        }
        return this;
    }

    /**
     * Setter a participant of the project by Cristin id, name or part of the name. [withDuplicates]
     */
    public QueryBuilderProject withParticipant(String participant) {
        var participants =
            query.containsKey(PARTICIPANT)
                ? query.getValue(PARTICIPANT) + "," + participant
                : participant;
        query.setValue(PARTICIPANT, participants);
        return this;
    }

    /**
     * Setter Identity.
     */
    public QueryBuilderProject withPathIdentity(String identity) {
        if (nonNull(identity) && !identity.isBlank()) {
            if (identity.matches(PATH_ORGANISATION.getPattern())) {
                query.setPath(PATH_ORGANISATION, identity);
            } else if (identity.matches(PATH_PROJECT.getPattern())) {
                query.setPath(PATH_PROJECT, identity);
            } else {
                query.setPath(PATH_IDENTITY, identity);
            }
        } else {
            query.setPath(PATH_IDENTITY, EMPTY_STRING);
        }
        return this;
    }

    /**
     * Setter Organization.
     */
    public QueryBuilderProject withPathOrganization(String organization) {
        if (nonNull(organization)) {
            if (organization.matches(ORGANIZATION.getPattern())) {
                query.setValue(ORGANIZATION, organization);
            } else {
                query.setPath(PATH_ORGANISATION, organization);
            }
        } else {
            query.setValue(PATH_ORGANISATION, EMPTY_STRING);
        }
        return this;
    }

    /**
     * Setter Project(identity) .
     */
    public QueryBuilderProject withPathProject(String project) {
        query.setPath(PATH_PROJECT, project);
        return this;
    }


    /**
     * Setter project manager by Cristin id, name or part of the name.
     */
    public QueryBuilderProject withProjectManager(String projectManager) {
        query.setValue(PROJECT_MANAGER, projectManager);
        return this;
    }

    /**
     * Setter Query.
     */
    public QueryBuilderProject withQuery(String queryValue) {
        if (nonNull(queryValue)) {
            var key = grantOrTitleKey(queryValue);
            query.setValue(key, queryValue);
        } else {
            query.setValue(QUERY, EMPTY_STRING);
        }
        return this;
    }

    /**
     * Setter status of projects.
     */
    public QueryBuilderProject withStatus(String status) {
        var statusKind = attempt(() ->
                                     ProjectStatus.valueOf(status.toUpperCase(Locale.getDefault())).name())
                             .orElse((e) -> EMPTY_STRING);
        query.setValue(STATUS, statusKind);
        return this;
    }

    /**
     * Setter Biobank id.
     */
    public QueryBuilderProject withTitle(String title) {
        query.setValue(TITLE, title);
        return this;
    }

    /**
     * Setter unit id.
     */
    public QueryBuilderProject withUnit(String unit) {
        query.setValue(PROJECT_UNIT, unit);
        return this;
    }

    /**
     * Setter a person's username in Cristin together with the institution id separated by ':'.
     */
    public QueryBuilderProject withUser(String user) {
        query.setValue(USER, user);
        return this;
    }

    /**
     * End Setters * Start private functions.
     */
    private ParameterKeyProject grantOrTitleKey(String query) {
        return Utils.isPositiveInteger(query) ? GRANT_ID : TITLE;
    }
}
