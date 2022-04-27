package no.unit.nva.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.StringUtils;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.language.LanguageConstants.UNDEFINED_LANGUAGE;
import static no.unit.nva.language.LanguageMapper.getLanguageByIso6391Code;
import static no.unit.nva.language.LanguageMapper.getLanguageByUri;
import static nva.commons.core.attempt.Try.attempt;

public class PatchValidator {

    public static final String FIELD_CAN_NOT_BE_ERASED = "Field %s can not be erased";
    public static final String ILLEGAL_VALUE_FOR_PROPERTY = "Illegal value for '%s'";
    public static final String RESERVED_FIELD_CAN_ONLY_BE_SET_TO_TRUE =
            "Reserved field can only be set to true if present";

    /**
     * Validate a map of language and String content.
     *
     * @param input        JsonNode containing map of language key and String content
     * @param propertyName witch property in ObjectNode contains the language and string map
     * @throws BadRequestException when input is not valid
     */
    public static void validateLanguageStringMap(ObjectNode input, String propertyName) throws BadRequestException {
        validateNotNullIfPresent(input, propertyName);
        TypeReference<Map<String, String>> typeRef = new TypeReference<>() {
        };
        attempt(() -> validateJsonProperty(input, propertyName, typeRef))
                .orElseThrow(fail -> new BadRequestException(String.format(ILLEGAL_VALUE_FOR_PROPERTY, propertyName)));
    }

    private static Void validateJsonProperty(ObjectNode input,
                                             String propertyName,
                                             TypeReference<Map<String, String>> typeRef)
            throws JsonProcessingException, BadRequestException {
        Map<String, String> languageAndStringMap = OBJECT_MAPPER.readValue(input.get(propertyName).asText(), typeRef);
        for (String languageCode : languageAndStringMap.keySet()) {
            validateLanguageCode(languageCode);
            validateLanguageContent(languageAndStringMap.get(languageCode), propertyName);
        }
        return null;
    }

    private static void validateLanguageContent(String content, String propertyName) throws BadRequestException {
        if (StringUtils.isEmpty(content)) {
            throw new BadRequestException(String.format(FIELD_CAN_NOT_BE_ERASED, propertyName));
        }
    }

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
     * Check if languageCode is valid ISO 6391 code.
     *
     * @param languageCode String whith language code
     * @throws BadRequestException thrown when language code is not valid
     */
    public static void validateLanguageCode(String languageCode) throws BadRequestException {
        if (getLanguageByIso6391Code(languageCode).equals(UNDEFINED_LANGUAGE)) {
            throw new BadRequestException(String.format(ILLEGAL_VALUE_FOR_PROPERTY, LANGUAGE));
        }
    }

    /**
     * Check if input contains a valid language URI.
     *
     * @param jsonNode JsonNode containing language property
     * @throws BadRequestException thrown when language uri is not valid
     */
    public static void validateLanguageUri(JsonNode jsonNode) throws BadRequestException {
        if (getLanguageByUri(URI.create(jsonNode.textValue())).equals(UNDEFINED_LANGUAGE)) {
            throw new BadRequestException(String.format(ILLEGAL_VALUE_FOR_PROPERTY, LANGUAGE));
        }
    }

    protected static void validateInstantIfPresent(ObjectNode input, String propertyName) throws BadRequestException {
        if (propertyHasValue(input, propertyName)) {
            attempt(() -> Instant.parse(input.get(propertyName).asText()))
                    .orElseThrow(fail -> new BadRequestException(String.format(ILLEGAL_VALUE_FOR_PROPERTY, propertyName)));
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
            throw new BadRequestException(String.format(FIELD_CAN_NOT_BE_ERASED, propertyName));
        }
    }

    public static boolean propertyHasValue(ObjectNode input, String propertyName) {
        return input.has(propertyName) && Objects.nonNull(input.get(propertyName));
    }
}
