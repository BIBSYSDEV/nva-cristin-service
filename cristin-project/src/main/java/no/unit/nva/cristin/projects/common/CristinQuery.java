package no.unit.nva.cristin.projects.common;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessage;
import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessageWithRange;
import static no.unit.nva.cristin.common.ErrorMessages.requiredMissingMessage;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.common.handler.CristinHandler.DEFAULT_LANGUAGE_CODE;
import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PROJECTS_PATH;
import static no.unit.nva.cristin.model.Constants.PROJECT_PATH_NVA;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.BIOBANK;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.FUNDING;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.FUNDING_SOURCE;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.GRANT_ID;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.IGNORE_PATH_PARAMETER_INDEX;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.INSTITUTION;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.INVALID;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.LANGUAGE;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.LEVELS;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.NAME;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.PAGE_CURRENT;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.PAGE_ITEMS_PER_PAGE;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.PAGE_SORT;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.PATH_ORGANISATION;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.PATH_PROJECT;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.PROJECT_APPROVAL_REFERENCE_ID;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.PROJECT_APPROVED_BY;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.PROJECT_KEYWORD;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.PROJECT_MANAGER;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.PROJECT_MODIFIED_SINCE;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.PROJECT_ORGANIZATION;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.PROJECT_PARTICIPANT;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.PROJECT_UNIT;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.QUERY;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.STATUS;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.TITLE;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.USER;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.VALID_QUERY_PARAMETERS;
import static no.unit.nva.cristin.model.Constants.QueryParameterKey.VALID_QUERY_PARAMETERS_KEYS;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.paths.UriWrapper;

@SuppressWarnings({"Unused", "LooseCoupling"})
public class CristinQuery {

    private final Map<QueryParameterKey, String> queryParameters;
    private final Map<QueryParameterKey, String> pathParameters;
    private final Set<QueryParameterKey> otherRequiredKeys;
    private boolean hasIdentity;
    private boolean isNvaQuery;

    private CristinQuery() {
        queryParameters = new ConcurrentHashMap<>();
        pathParameters = new ConcurrentHashMap<>();
        otherRequiredKeys = new HashSet<>();
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
                   .addChild(PROJECTS_PATH)
                   .addChild(id)
                   .addQueryParameters(Map.of(LANGUAGE.getKey(), language))
                   .getUri();
    }


    /**
     * Builds URI to search Cristin projects based on parameters supplied to the builder methods.
     * @return an URI to NVA (default) Projects with parameters.
     */
    public URI toURI() {
        return
            UriWrapper.fromUri(CRISTIN_API_URL)
                .addChild(PROJECTS_PATH)
                .addQueryParameters(toParameters())
                .getUri();
    }

    /**
     * Builds URI to search Cristin projects based on parameters supplied to the builder methods.
     *
     * @return an URI to Cristin/NVA Projects with parameters.
     */
    public URI toNvaURI() {
        return
            new UriWrapper(HTTPS, DOMAIN_NAME)
                .addChild(BASE_PATH)
                .addChild(getNvaPathAsArray())
                .addQueryParameters(toNvaParameters())
                .getUri();
    }

    /**
     * NVA Query Parameters with string Keys.
     * @return Map
     */
    public Map<String, String> toNvaParameters() {
        var results =
            queryParameters.entrySet().stream()
                .filter(this::nvaParameterFilter)
                .collect(Collectors.toMap(this::toNvaQueryName, this::toQueryValue));
        return new TreeMap<>(results);
    }

    /**
     * Cristin Query Parameters with string Keys.
     * @return Map of String and String
     */
    public Map<String, String> toParameters() {
        var results =
            Stream.of(queryParameters.entrySet(), pathParameters.entrySet())
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(this::toCristinQueryName,this::toQueryValue));
        return new TreeMap<>(results);
    }

    /**
     * Get value from Query Parameter Map with key.
     * @param key to look up.
     * @return String content raw
     */
    public String getValue(QueryParameterKey key) {
        return queryParameters.get(key);
    }

    /**
     * Add a key value pair to Query Parameter Map.
     * @param key to add to.
     * @param value to assign
     */
    public void setValue(QueryParameterKey key, String value) {
        if (nonNull(value) && key.isEncode()) {
            queryParameters.put(key, decodeUTF(value));
        } else {
            queryParameters.put(key, value);
        }
    }

    /**
     * Builds URI to search Cristin projects based on parameters supplied to the builder methods.
     * @param key to add to.
     * @param value to assign
     */
    public void setPath(QueryParameterKey key, String value) {
        if (nonNull(value) && key.isEncode()) {
            pathParameters.put(key, decodeUTF(value));
        } else {
            pathParameters.put(key, value);
        }
    }

    /**
     * Query Parameter map contain key.
     * @param key to check
     * @return true if map contains key.
     */
    public boolean containsKey(QueryParameterKey key) {
        return queryParameters.containsKey(key) || pathParameters.containsKey(key);
    }

    /**
     * Compares content of queryParameters in CristinQuery.
     * @param other CristinQuery to compare
     * @return true if content of Maps are equal
     */
    public boolean areEqual(CristinQuery other) {
        if (queryParameters.size() != other.queryParameters.size()
            || pathParameters.size() != other.pathParameters.size()) {
            return false;
        }

        return
            queryParameters.entrySet().stream()
                .allMatch(e -> e.getValue().equals(other.getValue(e.getKey())))
            &&
            pathParameters.entrySet().stream()
                .allMatch(e -> e.getValue().equals(other.getValue(e.getKey())));
    }

    private String[] getNvaPathAsArray() {
        var pathParams =
            this.pathParameters
                .entrySet()
                .stream()
                .flatMap(entry -> Stream.of(entry.getKey().getNvaKey(), entry.getValue()))
                .collect(Collectors.toList());

        if (pathParams.isEmpty()) {
            pathParams.add(PROJECT_PATH_NVA);
        } else if (!pathParams.contains(PROJECT_PATH_NVA)) {
            pathParams.add(PROJECTS_PATH);
        }
        return pathParams.toArray(new String[0]);
    }

    private String toNvaQueryName(Map.Entry<QueryParameterKey, String> entry) {
        return entry.getKey().getNvaKey();
    }

    private String toCristinQueryName(Map.Entry<QueryParameterKey, String> entry) {
        return entry.getKey().getKey();
    }

    private String toQueryValue(Map.Entry<QueryParameterKey, String> entry) {
        return entry.getKey().isEncode()
                   ? encodeUTF(entry.getValue())
                   : entry.getValue();
    }

    private String encodeUTF(String unencoded) {
        return URLEncoder.encode(unencoded, StandardCharsets.UTF_8).replace("%20","+");
    }

    private String decodeUTF(String encoded) {
        return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
    }

    private boolean nvaParameterFilter(Map.Entry<QueryParameterKey, String> entry) {
        return entry.getKey().ordinal() > IGNORE_PATH_PARAMETER_INDEX;
    }

    /**
     * Builds Cristin query parameters using builder methods and NVA input parameters.
     */
    public static final class Builder {

        private static final String PARAMETER_PAGE_DEFAULT_VALUE = "1";
        private static final String PARAMETER_PER_PAGE_DEFAULT_VALUE = "5";
        private final Set<String> invalidKeys = new HashSet<>(0);

        private final CristinQuery cristinQuery;

        /**
         * Constructor of CristinQuery.Builder.
         * <p>Usage:</p>
         * <samp>new CristinQuery.Builder()<br>
         * .fromRequestInfo(requestInfo)<br>
         * .withRequiredParameters(IDENTITY,PAGE_CURRENT,PAGE_ITEMS_PER_PAGE)<br>
         * .validate()<br>.build()
         * </samp>
         */
        public Builder() {
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
        public Builder validate() throws BadRequestException {
            assignDefaultValues();
            for (Entry<QueryParameterKey, String> entry : cristinQuery.pathParameters.entrySet()) {
                throwInvalidParamererValue(entry.getKey(), entry.getValue());
            }
            for (Entry<QueryParameterKey, String> entry : cristinQuery.queryParameters.entrySet()) {
                throwInvalidParamererValue(entry.getKey(), entry.getValue());
            }
            if (!requiredMissing().isEmpty()) {
                throw new BadRequestException(requiredMissingMessage(getMissingKeys()));
            }
            if (oneOrMoreOptionalIsMissing()) {
                throw new BadRequestException(validQueryParameterNamesMessage(VALID_QUERY_PARAMETERS_KEYS));
            }
            if (!invalidKeys.isEmpty()) {
                throw new BadRequestException(validQueryParameterNamesMessage(VALID_QUERY_PARAMETERS_KEYS));
            }
            return this;
        }

        private Set<String> getMissingKeys() {
            return
                requiredMissing()
                    .stream()
                    .map(QueryParameterKey::getKey)
                    .collect(Collectors.toSet());
        }

        /**
         * Adds query and path parameters from requestInfo.
         */
        public Builder fromRequestInfo(RequestInfo requestInfo) {
            return fromPathParameters(requestInfo.getPathParameters())
                   .fromQueryParameters(requestInfo.getQueryParameters());
        }

        /**
         * Adds parameters from query.
         */
        public Builder fromQueryParameters(Map<String, String> parameters) {
            parameters.forEach(this::setValue);
            return this;
        }

        /**
         * Adds parameters from path.
         * */
        public Builder fromPathParameters(Map<String, String> parameters) {
            parameters.forEach(this::setPath);
            return this;
        }

        /**
         * Defines which parameters are required.
         * @param requiredParameters comma seperated QueryParameterKeys
         */
        public Builder withRequiredParameters(QueryParameterKey... requiredParameters) {
            var tmpSet = Set.of(requiredParameters);
            cristinQuery.hasIdentity = !cristinQuery.pathParameters.isEmpty();
            cristinQuery.otherRequiredKeys.addAll(
                tmpSet.stream()
                    .filter(key -> !cristinQuery.pathParameters.containsKey(key))
                    .collect(Collectors.toSet()));
            return this;
        }

        /**
         * Setter Organization.
         */
        public Builder withPathOrganization(String organization) {
            if (nonNull(organization)) {
                if (organization.matches(PROJECT_ORGANIZATION.getPattern())) {
                    cristinQuery.setValue(PATH_ORGANISATION,organization);
                } else {
                    cristinQuery.setPath(PATH_ORGANISATION, organization);
                }
            }
            return this;
        }

        /**
         * Setter Identity.
         */
        public Builder withPathIdentity(String identity) {
            if (nonNull(identity)) {
                if (identity.matches(PATH_PROJECT.getPattern())) {
                    cristinQuery.setPath(PATH_PROJECT, identity);
                } else if (identity.matches(PATH_ORGANISATION.getPattern())) {
                    cristinQuery.setPath(PATH_ORGANISATION, identity);
                }
            }
            return this;
        }

        public Builder withIdentity(String identifier) {
            return withPathIdentity(identifier);
        }


        /**
         * Builder GRANT ID..
         */
        public Builder withGrantId(String grantId) {
            cristinQuery.setValue(GRANT_ID, grantId);
            return this;
        }

        /**
         * Preferred language.
         */
        public Builder withLanguage(String language) {
            var langOrDefault = nonNull(language)
                                    ? language
                                    : DEFAULT_LANGUAGE_CODE;
            cristinQuery.setValue(LANGUAGE, langOrDefault);
            return this;
        }

        /**
         * Start of pagination.
         */
        public Builder withItemsFromPage(String page) {
            cristinQuery.setValue(PAGE_CURRENT, page);
            return this;
        }

        /**
         * Items per page.
         */
        public Builder withItemsPerPage(String itemsPerPage) {
            cristinQuery.setValue(PAGE_ITEMS_PER_PAGE, itemsPerPage);
            return this;
        }


        /**
         * Setter status of projects.
         */
        public Builder withStatus(String status) {
            if (nonNull(status)) {
                cristinQuery.setValue(STATUS, status.toUpperCase(Locale.getDefault()));
            }
            return this;
        }

        /**
         * Setter Biobank id.
         */
        public Builder withTitle(String title) {
            cristinQuery.setValue(TITLE, title);
            return this;
        }

        /**
         * Setter coordinating institution by cristin id, acronym, name, or part of the name.
         */
        public Builder withInstitution(String institution) {
            cristinQuery.setValue(INSTITUTION, institution);
            return this;
        }

        /**
         * Setter project manager by Cristin id, name or part of the name.
         */
        public Builder withProjectManager(String projectManager) {
            cristinQuery.setValue(PROJECT_MANAGER, projectManager);
            return this;
        }

        /**
         * Setter a participant of the project by Cristin id, name or part of the name.
         */
        public Builder withParticipant(String participant) {
            cristinQuery.setValue(PROJECT_PARTICIPANT, participant);
            return this;
        }

        /**
         * Setter a participant of the project by Cristin id, name or part of the name.
         */
        public Builder withParentUnitId(String parentUnitId) {
            cristinQuery.setPath(PATH_ORGANISATION,parentUnitId);
            return this;
        }

        /**
         * Setter search by keyword.
         */
        public Builder withKeyword(String keyword) {
            cristinQuery.setValue(PROJECT_KEYWORD, keyword);
            return this;
        }

        /**
         * Setter funding source code.
         */
        public Builder withFundingSource(String fundingSource) {
            cristinQuery.setValue(FUNDING_SOURCE, fundingSource);
            return this;
        }

        /**
         * Setter reference id of project approval.
         */
        public Builder withApprovalReferenceId(String approvalReferenceId) {
            cristinQuery.setValue(PROJECT_APPROVAL_REFERENCE_ID, approvalReferenceId);
            return this;
        }

        /**
         * Setter sorting on 'start_date' and/or 'end_date'.
         */
        public Builder withItemSort(String sort) {
            cristinQuery.setValue(PAGE_SORT, sort);
            return this;
        }

        /**
         * Setter unit id.
         */
        public Builder withUnit(String unit) {
            cristinQuery.setValue(PROJECT_UNIT, unit);
            return this;
        }

        /**
         * Setter a person's username in Cristin together with the institution id separated by ':'.
         */
        public Builder withUser(String user) {
            cristinQuery.setValue(USER, user);
            return this;
        }

        /**
         * Setter code for the authority that evaluated a project approval.
         */
        public Builder withApprovedBy(String approvedby) {
            cristinQuery.setValue(PROJECT_APPROVED_BY, approvedby);
            return this;
        }

        /**
         * Setter funding source code e.g: NFR, and project_code together separated by ':'
         */
        public Builder withFunding(String funding) {
            cristinQuery.setValue(FUNDING, funding);
            return this;
        }

        /**
         * Setter how many levels down from 'parent_unit_id' will be included in the search.
         */
        public Builder withLevels(String levels) {
            cristinQuery.setValue(LEVELS, levels);
            return this;
        }

        /**
         * Setter only those projects that have been modified since this date will be returned.
         */
        public Builder withModifiedSince(String modifiedSince) {
            cristinQuery.setValue(PROJECT_MODIFIED_SINCE, modifiedSince);
            return this;
        }

        /**
         * Setter Name.
         */
        public Builder withName(String name) {
            cristinQuery.setValue(NAME, name);
            return this;
        }

        /**
         * Setter Biobank id.
         */
        public Builder withBiobank(String biobank) {
            cristinQuery.setValue(BIOBANK, biobank);
            return this;
        }

        /**
         * Setter Query.
         */
        public Builder withQuery(String query) {
            if (nonNull(query)) {
                var key = grantOrTitleKey(query);
                cristinQuery.setValue(key, query);
            } else {
                cristinQuery.setValue(QUERY, query);
            }
            return this;
        }

        /**
         * Setter Organization.
         */
        public Builder withOrganization(String organization) {
            if (nonNull(organization)) {
                cristinQuery.setValue(PROJECT_ORGANIZATION, organization);
            }
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
            if (nonNull(value)) {
                var qpKey = QueryParameterKey.fromString(key,value);
                switch (qpKey) {
                    case IDENTITY:
                    case PATH_PROJECT:
                        withPathIdentity(value);
                        break;
                    case PATH_ORGANISATION:
                        withPathOrganization(value);
                        break;
                    default:
                        invalidKeys.add(key);
                        break;
                }
            }
        }

        private void setValue(String key, String value) {
            var qpKey = QueryParameterKey.fromString(key,value);
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
            return oneOrMoreOptionalUnassigned().size() == oneOrMoreOptional().size() && !cristinQuery.hasIdentity;
        }

        private Set<QueryParameterKey> oneOrMoreOptionalUnassigned() {
            return VALID_QUERY_PARAMETERS.stream()
                       .filter(key -> !cristinQuery.containsKey(key)
                                      && !required().contains(key))
                       .collect(Collectors.toSet());
        }

        private Set<QueryParameterKey> oneOrMoreOptional() {
            return VALID_QUERY_PARAMETERS.stream()
                       .filter(key -> !required().contains(key))
                       .collect(Collectors.toSet());
        }

        private Set<QueryParameterKey> required() {
            return cristinQuery.otherRequiredKeys;
        }

        private Set<QueryParameterKey> requiredMissing() {
            return required().stream()
                       .filter(key -> !cristinQuery.queryParameters.containsKey(key))
                       .filter(key -> !cristinQuery.pathParameters.containsKey(key))
                       .collect(Collectors.toSet());
        }

        private QueryParameterKey grantOrTitleKey(String query) {
            return Utils.isPositiveInteger(query) ? GRANT_ID : TITLE;
        }

        private String getUnitIdFromOrganization(String organizationId) {
            return extractLastPathElement(URI.create(organizationId));
        }

        private void throwInvalidParamererValue(QueryParameterKey key, String value) throws BadRequestException {
            if (invalidQueryParameter(key, value)) {
                var keyname = cristinQuery.isNvaQuery ? key.getNvaKey() : key.getKey();
                String errorMessage;
                if (key == STATUS) {
                    errorMessage =
                        invalidQueryParametersMessageWithRange(key.getKey(),Arrays.toString(ProjectStatus.values()));
                } else if (nonNull(key.getErrorMessage())) {
                    errorMessage =
                        invalidQueryParametersMessage(keyname, key.getErrorMessage());
                } else {
                    errorMessage =
                        invalidQueryParametersMessage(keyname, value);
                }
                throw new BadRequestException(errorMessage);
            }
        }

        private boolean invalidQueryParameter(QueryParameterKey key, String value) {
            return isNull(value) || !value.matches(key.getPattern());
        }
    }
}
