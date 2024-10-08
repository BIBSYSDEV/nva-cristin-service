package no.unit.nva.biobank.model;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_NUMBER;
import static no.unit.nva.cristin.model.Constants.CRISTIN_PER_PAGE_PARAM;
import static no.unit.nva.cristin.model.Constants.PATTERN_IS_LANGUAGE;
import static no.unit.nva.cristin.model.Constants.PATTERN_IS_NON_EMPTY;
import static no.unit.nva.cristin.model.Constants.PATTERN_IS_NUMBER;
import static no.unit.nva.cristin.model.Constants.QUERY_PARAMETER_LANGUAGE;
import java.util.Arrays;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import no.unit.nva.cristin.model.Constants;
import no.unit.nva.cristin.model.IParameterKey;
import no.unit.nva.cristin.model.JsonPropertyNames;
import no.unit.nva.cristin.model.KeyEncoding;

public enum ParameterKeyBiobank implements IParameterKey {
    INVALID(null),
    PATH_IDENTITY(JsonPropertyNames.IDENTIFIER, null, PATTERN_IS_NUMBER),
    PATH_BIOBANK("biobanks", JsonPropertyNames.BIOBANK_ID, PATTERN_IS_NUMBER),
    LANGUAGE(QUERY_PARAMETER_LANGUAGE, JsonPropertyNames.LANGUAGE, PATTERN_IS_LANGUAGE),
    STATUS(JsonPropertyNames.STATUS, null, Constants.PATTERN_IS_STATUS, null, KeyEncoding.DECODE),
    USER(JsonPropertyNames.USER),
    PAGE_CURRENT(JsonPropertyNames.PAGE, null, PATTERN_IS_NUMBER, ERROR_MESSAGE_INVALID_NUMBER, KeyEncoding.NONE),
    PAGE_ITEMS_PER_PAGE(CRISTIN_PER_PAGE_PARAM,
        JsonPropertyNames.NUMBER_OF_RESULTS,
        PATTERN_IS_NUMBER,
        ERROR_MESSAGE_INVALID_NUMBER,
        KeyEncoding.NONE),
    PAGE_SORT(JsonPropertyNames.PROJECT_SORT);

    public static final int IGNORE_PATH_PARAMETER_INDEX = 2;

    public static final Set<ParameterKeyBiobank> VALID_QUERY_PARAMETERS =
        Arrays.stream(values())
            .filter(ParameterKeyBiobank::ignorePathKeys)
            .collect(Collectors.toSet());

    public static final Set<String> VALID_QUERY_PARAMETER_NVA_KEYS =
        VALID_QUERY_PARAMETERS.stream()
            .sorted()
            .map(ParameterKeyBiobank::getNvaKey)
            .collect(Collectors.toSet());


    private final String pattern;
    private final String cristinKey;
    private final String nvaKey;
    private final KeyEncoding encode;
    private final String errorMessage;

    ParameterKeyBiobank(String cristinKey) {
        this(cristinKey, null, PATTERN_IS_NON_EMPTY, null, KeyEncoding.NONE);
    }

    ParameterKeyBiobank(String cristinKey, String nvaKey, String pattern) {
        this(cristinKey, nvaKey, pattern, null, KeyEncoding.NONE);
    }

    ParameterKeyBiobank(String cristinKey, String nvaKey, String pattern, String errorMessage,
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

    public static ParameterKeyBiobank keyFromString(String paramName, String value) {
        return Arrays.stream(values())
                .filter(ParameterKeyBiobank::ignorePathKeys)
                .filter(IParameterKey.equalTo(paramName))
                .distinct()
                .reduce((first, second) -> IParameterKey.hasValidValue(value).test(second) ? second : first)
                .orElse(INVALID);
    }


    private static boolean ignorePathKeys(ParameterKeyBiobank f) {
        return f.ordinal() > IGNORE_PATH_PARAMETER_INDEX;
    }

}