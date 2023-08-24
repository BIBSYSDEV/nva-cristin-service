package no.unit.nva.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import nva.commons.apigateway.exceptions.BadRequestException;

import java.net.URI;
import java.time.Instant;
import java.util.Objects;

import static java.lang.String.format;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.language.LanguageConstants.UNDEFINED_LANGUAGE;
import static no.unit.nva.language.LanguageMapper.getLanguageByUri;
import static nva.commons.core.attempt.Try.attempt;

public class PatchValidator {

    public static final String FIELD_CAN_NOT_BE_ERASED = "Field %s can not be erased";
    public static final String ILLEGAL_VALUE_FOR_PROPERTY = "Illegal value for '%s'";
    public static final String COULD_NOT_PARSE_LANGUAGE_FIELD = "Could not parse language field";
    public static final String NOT_A_VALID_KEY_VALUE_FIELD = "%s not a valid key value field";

    /**
     * Verifies input contains a valid language property.
     *
     * @param input ObjectNode containing language property
     * @throws BadRequestException thrown when language property is not valid
     */
    public static void validateLanguage(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, LANGUAGE);
        if (propertyHasValue(input, LANGUAGE)) {
            validateLanguageUri(input.get(LANGUAGE));
        }
    }

    /**
     * Check if input contains a valid language URI.
     *
     * @param jsonNode JsonNode containing language property
     * @throws BadRequestException thrown when language uri is not valid
     */
    public static void validateLanguageUri(JsonNode jsonNode) throws BadRequestException {
        var languageUri = attempt(() -> URI.create(jsonNode.textValue()))
                              .orElseThrow(failure -> new BadRequestException(COULD_NOT_PARSE_LANGUAGE_FIELD));
        if (UNDEFINED_LANGUAGE.equals(getLanguageByUri(languageUri))) {
            throw new BadRequestException(format(ILLEGAL_VALUE_FOR_PROPERTY, LANGUAGE));
        }
    }

    protected static void validateInstantIfPresent(ObjectNode input, String propertyName) throws BadRequestException {
        if (propertyHasValue(input, propertyName)) {
            attempt(() -> Instant.parse(input.get(propertyName).asText()))
                .orElseThrow(fail -> new BadRequestException(format(ILLEGAL_VALUE_FOR_PROPERTY, propertyName)));
        }
    }

    /**
     * Verify a field has value if present in input.
     *
     * @param input        ObjectNode containing some properties
     * @param propertyName property containing value tho be verified
     * @throws BadRequestException when property is invalid
     */
    public static void validateNotNullIfPresent(ObjectNode input, String propertyName) throws BadRequestException {
        if (input.has(propertyName) && input.get(propertyName).isNull()) {
            throw new BadRequestException(format(FIELD_CAN_NOT_BE_ERASED, propertyName));
        }
    }

    public static boolean propertyHasValue(ObjectNode input, String propertyName) {
        return input.has(propertyName) && Objects.nonNull(input.get(propertyName));
    }

    public static void validateDescription(ObjectNode input, String fieldName) throws BadRequestException {
        if (input.has(fieldName)) {
            var description = input.get(fieldName);
            attempt(() -> convertToMap(description)).orElseThrow(failure -> notAMapException(fieldName));
        }
    }

    public static Map<String, String> convertToMap(JsonNode node) {
        return OBJECT_MAPPER.convertValue(node, new TypeReference<>() {});
    }

    private static BadRequestException notAMapException(String fieldName) {
        return new BadRequestException(format(NOT_A_VALID_KEY_VALUE_FIELD, fieldName));
    }
}
