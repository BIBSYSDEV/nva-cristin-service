package no.unit.nva.cristin.projects.common;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_NUMBER;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_VALUE;
import static no.unit.nva.cristin.common.ErrorMessages.INVALID_URI_MESSAGE;
import static no.unit.nva.cristin.model.Constants.CATEGORY_PARAM;
import static no.unit.nva.cristin.model.Constants.CRISTIN_PER_PAGE_PARAM;
import static no.unit.nva.cristin.model.Constants.CRISTIN_QUERY_NAME_PARAM;
import static no.unit.nva.cristin.model.Constants.PARENT_UNIT_ID;
import static no.unit.nva.cristin.model.Constants.PATTERN_IS_DATE;
import static no.unit.nva.cristin.model.Constants.PATTERN_IS_LANGUAGE;
import static no.unit.nva.cristin.model.Constants.PATTERN_IS_NON_EMPTY;
import static no.unit.nva.cristin.model.Constants.PATTERN_IS_NUMBER;
import static no.unit.nva.cristin.model.Constants.PATTERN_IS_TITLE;
import static no.unit.nva.cristin.model.Constants.PATTERN_IS_URL;
import static no.unit.nva.cristin.model.Constants.PROJECTS_PATH;
import static no.unit.nva.cristin.model.Constants.PROJECT_CREATOR_PARAM;
import static no.unit.nva.cristin.model.Constants.PROJECT_PATH_NVA;
import static no.unit.nva.cristin.model.Constants.QUERY_PARAMETER_LANGUAGE;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.QueryParameterConstant.ERROR_MESSAGE_INVALID_CHARACTERS;
import static no.unit.nva.model.Organization.ORGANIZATION_IDENTIFIER_PATTERN;
import java.util.Arrays;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import no.unit.nva.cristin.model.Constants;
import no.unit.nva.cristin.model.IParameterKey;
import no.unit.nva.cristin.model.JsonPropertyNames;
import no.unit.nva.cristin.model.KeyEncoding;
import no.unit.nva.cristin.model.query.CristinFacetParamKey;

public enum ParameterKeyProject implements IParameterKey {
    INVALID(null),
    PATH_IDENTITY(JsonPropertyNames.IDENTIFIER, null, PATTERN_IS_NON_EMPTY),
    PATH_ORGANISATION(PARENT_UNIT_ID,
        JsonPropertyNames.ORGANIZATION,
        ORGANIZATION_IDENTIFIER_PATTERN,
        ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS,
        KeyEncoding.DECODE),
    PATH_PROJECT(PROJECTS_PATH, PROJECT_PATH_NVA, PATTERN_IS_NUMBER),
    APPROVAL_REFERENCE_ID(JsonPropertyNames.PROJECT_APPROVAL_REFERENCE_ID),
    APPROVED_BY(JsonPropertyNames.PROJECT_APPROVED_BY),
    BIOBANK(JsonPropertyNames.BIOBANK_ID),
    FUNDING(JsonPropertyNames.FUNDING),
    FUNDING_SOURCE(JsonPropertyNames.FUNDING_SOURCE),
    GRANT_ID("project_code", null, PATTERN_IS_NUMBER),
    INSTITUTION(JsonPropertyNames.INSTITUTION),
    KEYWORD(JsonPropertyNames.PROJECT_KEYWORD),
    LANGUAGE(QUERY_PARAMETER_LANGUAGE, JsonPropertyNames.LANGUAGE, PATTERN_IS_LANGUAGE),
    LEVELS(JsonPropertyNames.LEVELS, JsonPropertyNames.DEPTH, PATTERN_IS_NUMBER),
    MODIFIED_SINCE(JsonPropertyNames.PROJECT_MODIFIED_SINCE, null, PATTERN_IS_DATE),
    NAME(JsonPropertyNames.TITLE,
         CRISTIN_QUERY_NAME_PARAM,
         PATTERN_IS_TITLE,
         String.format(ERROR_MESSAGE_INVALID_CHARACTERS,JsonPropertyNames.TITLE),
        KeyEncoding.ENCODE_DECODE),
    ORGANIZATION(PARENT_UNIT_ID,JsonPropertyNames.ORGANIZATION,PATTERN_IS_URL,INVALID_URI_MESSAGE,KeyEncoding.DECODE),
    PARTICIPANT(JsonPropertyNames.PROJECT_PARTICIPANT),
    PROJECT_MANAGER(JsonPropertyNames.PROJECT_MANAGER),
    PROJECT_UNIT(JsonPropertyNames.UNIT),
    QUERY(JsonPropertyNames.QUERY,
          null,
          PATTERN_IS_NON_EMPTY,
          String.format(ERROR_MESSAGE_INVALID_CHARACTERS,JsonPropertyNames.QUERY),
        KeyEncoding.ENCODE_DECODE),
    STATUS(JsonPropertyNames.STATUS, null, Constants.PATTERN_IS_STATUS, null, KeyEncoding.DECODE),
    TITLE(JsonPropertyNames.TITLE,
        null,
        PATTERN_IS_TITLE,
        String.format(ERROR_MESSAGE_INVALID_CHARACTERS, JsonPropertyNames.TITLE),
        KeyEncoding.ENCODE_DECODE),
    USER(JsonPropertyNames.USER),
    PAGE_CURRENT(JsonPropertyNames.PAGE, null, PATTERN_IS_NUMBER, ERROR_MESSAGE_INVALID_NUMBER, KeyEncoding.NONE),
    PAGE_ITEMS_PER_PAGE(CRISTIN_PER_PAGE_PARAM,
        JsonPropertyNames.NUMBER_OF_RESULTS,
        PATTERN_IS_NUMBER,
        ERROR_MESSAGE_INVALID_NUMBER,
        KeyEncoding.NONE),
    PAGE_SORT(JsonPropertyNames.PROJECT_SORT),
    CREATOR(PROJECT_CREATOR_PARAM, null, PATTERN_IS_NUMBER, ERROR_MESSAGE_INVALID_NUMBER, KeyEncoding.NONE),
    CATEGORY(CATEGORY_PARAM),
    // Facets from here onward
    SECTOR_FACET(CristinFacetParamKey.SECTOR_PARAM.getKey(), CristinFacetParamKey.SECTOR_PARAM.getNvaKey()),
    COORDINATING_FACET(CristinFacetParamKey.COORDINATING_PARAM.getKey(),
                       CristinFacetParamKey.COORDINATING_PARAM.getNvaKey()),
    RESPONSIBLE_FACET(CristinFacetParamKey.RESPONSIBLE_PARAM.getKey(),
                      CristinFacetParamKey.RESPONSIBLE_PARAM.getNvaKey()),
    CATEGORY_FACET(CristinFacetParamKey.CATEGORY_PARAM.getKey(), CristinFacetParamKey.CATEGORY_PARAM.getNvaKey()),
    HEALTH_FACET(CristinFacetParamKey.HEALTH_PARAM.getKey(), CristinFacetParamKey.HEALTH_PARAM.getNvaKey()),
    PARTICIPANT_FACET(CristinFacetParamKey.PARTICIPANT_PARAM.getKey(),
                      CristinFacetParamKey.PARTICIPANT_PARAM.getNvaKey()),
    PARTICIPATING_PERSON_ORG_FACET(CristinFacetParamKey.PARTICIPATING_PERSON_ORG_PARAM.getKey(),
                                   CristinFacetParamKey.PARTICIPATING_PERSON_ORG_PARAM.getNvaKey()),
    FUNDING_SOURCE_FACET(CristinFacetParamKey.FUNDING_SOURCE_PARAM.getKey(),
                         CristinFacetParamKey.FUNDING_SOURCE_PARAM.getNvaKey());

    public static final int IGNORE_PATH_PARAMETER_INDEX = 3;
    public static final int IGNORE_FACET_PARAMETER_INDEX = 29;

    public static final Set<ParameterKeyProject> VALID_QUERY_PARAMETERS =
        Arrays.stream(ParameterKeyProject.values())
            .filter(ParameterKeyProject::ignorePathKeys)
            .collect(Collectors.toSet());

    public static final Set<String> VALID_QUERY_PARAMETER_KEYS =
        VALID_QUERY_PARAMETERS.stream()
            .sorted()
            .map(ParameterKeyProject::getKey)
            .collect(Collectors.toSet());

    public static final Set<String> VALID_QUERY_PARAMETER_NVA_KEYS =
        VALID_QUERY_PARAMETERS.stream()
            .filter(ParameterKeyProject::ignoreFacetKeys)
            .sorted()
            .map(ParameterKeyProject::getNvaKey)
            .collect(Collectors.toSet());

    public static final Set<String> VALID_QUERY_PARAMETER_NVA_KEYS_AND_FACETS =
        VALID_QUERY_PARAMETERS.stream()
            .sorted()
            .map(ParameterKeyProject::getNvaKey)
            .collect(Collectors.toSet());

    private final String pattern;
    private final String cristinKey;
    private final String nvaKey;
    private final KeyEncoding encode;
    private final String errorMessage;

    ParameterKeyProject(String cristinKey) {
        this(cristinKey, null, PATTERN_IS_NON_EMPTY, null, KeyEncoding.NONE);
    }

    ParameterKeyProject(String cristinKey, String nvaKey) {
        this(cristinKey, nvaKey, PATTERN_IS_NON_EMPTY, null, KeyEncoding.NONE);
    }

    ParameterKeyProject(String cristinKey, String nvaKey, String pattern) {
        this(cristinKey, nvaKey, pattern, null, KeyEncoding.NONE);
    }

    ParameterKeyProject(String cristinKey, String nvaKey, String pattern, String errorMessage,
                        KeyEncoding encode) {
        this.cristinKey = cristinKey;
        this.nvaKey = nonNull(nvaKey) ? nvaKey : cristinKey;
        this.pattern = pattern;
        this.encode = encode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getNvaKey() {
        return nvaKey;
    }

    @Override
    public String getKey() {
        return cristinKey;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public KeyEncoding encoding() {
        return encode;
    }

    @Override
    public String toString() {
        return
            new StringJoiner(":", "Key[", "]")
                .add(String.valueOf(ordinal()))
                .add(name())
                .toString();
    }

    public static ParameterKeyProject keyFromString(String paramName, String value) {
        var result = Arrays.stream(ParameterKeyProject.values())
                         .filter(ParameterKeyProject::ignorePathKeys)
                         .filter(IParameterKey.equalTo(paramName))
                         .collect(Collectors.toSet());
        return result.size() == 1
                   ? result.stream().findFirst().get()
                   : result.stream()
                         .filter(IParameterKey.hasValidValue(value))
                         .findFirst()
                         .orElse(INVALID);
    }


    private static boolean ignorePathKeys(ParameterKeyProject f) {
        return f.ordinal() > IGNORE_PATH_PARAMETER_INDEX;
    }

    private static boolean ignoreFacetKeys(ParameterKeyProject keys) {
        return keys.ordinal() < IGNORE_FACET_PARAMETER_INDEX;
    }

    public static class QueryParameterConstant {
        public static final String ERROR_MESSAGE_INVALID_CHARACTERS =
            ERROR_MESSAGE_INVALID_VALUE + ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE;
    }
}