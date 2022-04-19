package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.StringUtils;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.ALTERNATIVE_TITLES;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTRIBUTORS;
import static no.unit.nva.cristin.model.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.POPULAR_SCIENTIFIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TITLE;
import static no.unit.nva.language.LanguageConstants.UNDEFINED_LANGUAGE;
import static no.unit.nva.language.LanguageMapper.getLanguageByIso6391Code;
import static no.unit.nva.language.LanguageMapper.getLanguageByUri;
import static nva.commons.core.attempt.Try.attempt;


public class ProjectPatchValidator {

    public static final String FIELD_CAN_NOT_BE_ERASED = "Field %s can not be erased";
    public static final String ILLEGAL_VALUE_FOR_PROPERTY = "Illegal value for '%s'";

    /**
     * Validate changes to Project, both nullable fields and values.
     * @param input ObjectNode containing fields with input data to be changed.
     * @throws BadRequestException thrown when input has illegal or invalid values.
     */
    public static void validate(ObjectNode input) throws BadRequestException {

        validateRequiredFieldsNotNull(input);

        validateLanguageStringMap(input, ACADEMIC_SUMMARY);
        validateAlternativeTitles(input);
        validateLanguageStringMap(input, POPULAR_SCIENTIFIC_SUMMARY);
        validateInstant(input, END_DATE);
        validateInstant(input, START_DATE);
        validateLanguage(input);
        validateStatus(input);
        validateContributors(input);
    }

    private static void validateRequiredFieldsNotNull(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, TITLE);
        validateNotNullIfPresent(input, START_DATE);
        validateNotNullIfPresent(input, CONTRIBUTORS);
        validateNotNullIfPresent(input, COORDINATING_INSTITUTION);
    }

    private static void validateContributors(ObjectNode input) throws BadRequestException {
        TypeReference<List<NvaContributor>> typeRef = new TypeReference<>() {
        };
        try {
            final String content = input.get(CONTRIBUTORS).asText();
            List<NvaContributor> contributors = OBJECT_MAPPER.readValue(content, typeRef);
            if (contributors.isEmpty()) {
                throw new BadRequestException(String.format(FIELD_CAN_NOT_BE_ERASED, CONTRIBUTORS));
            }
        } catch (JsonProcessingException e) {
            throw new BadRequestException(String.format(ILLEGAL_VALUE_FOR_PROPERTY, CONTRIBUTORS));
        }
    }

    private static void validateStatus(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, no.unit.nva.cristin.model.JsonPropertyNames.STATUS);
        ProjectStatus.isValidStatus(input.get(no.unit.nva.cristin.model.JsonPropertyNames.STATUS).asText());
    }

    private static void validateAlternativeTitles(ObjectNode input)
            throws BadRequestException {
        validateNotNullIfPresent(input,ALTERNATIVE_TITLES);
        TypeReference<List<Map<String, String>>> typeRef = new TypeReference<>() {
        };
        attempt(() -> OBJECT_MAPPER.readValue(input.get(ALTERNATIVE_TITLES).asText(), typeRef))
                .orElseThrow(fail ->
                        new BadRequestException(String.format(ILLEGAL_VALUE_FOR_PROPERTY, ALTERNATIVE_TITLES)));
    }

    private static void validateLanguageStringMap(ObjectNode input, String propertyName) throws BadRequestException {
        validateNotNullIfPresent(input, propertyName);
        TypeReference<Map<String, String>> typeRef = new TypeReference<>() {
        };
        try {
            Map<String, String> languageAndStringMap =
                    OBJECT_MAPPER.readValue(input.get(propertyName).asText(), typeRef);
            for (String languageCode : languageAndStringMap.keySet()) {
                validateLanguageCode(languageCode);
                validateLanguageContent(languageAndStringMap.get(languageCode), propertyName);
            }
        } catch (JsonProcessingException e) {
            throw new BadRequestException(String.format(ILLEGAL_VALUE_FOR_PROPERTY, propertyName));
        }
    }

    private static void validateLanguageContent(String content, String propertyName) throws BadRequestException {
        if (StringUtils.isEmpty(content)) {
            throw new BadRequestException(String.format(FIELD_CAN_NOT_BE_ERASED, propertyName));
        }
    }

    private static void validateLanguage(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, LANGUAGE);
        validateLanguageUri(input.get(LANGUAGE));
    }

    private static void validateLanguageCode(String languageCode) throws BadRequestException {
        if (getLanguageByIso6391Code(languageCode).equals(UNDEFINED_LANGUAGE)) {
            throw new BadRequestException(String.format(ILLEGAL_VALUE_FOR_PROPERTY, LANGUAGE));
        }
    }

    private static void validateLanguageUri(JsonNode jsonNode) throws BadRequestException {
        if (getLanguageByUri(URI.create(jsonNode.textValue())).equals(UNDEFINED_LANGUAGE)) {
            throw new BadRequestException(String.format(ILLEGAL_VALUE_FOR_PROPERTY, LANGUAGE));
        }
    }

    private static void validateInstant(ObjectNode input, String propertyName) throws BadRequestException {
        attempt(() -> Instant.parse(input.get(propertyName).asText()))
                .orElseThrow(fail -> new BadRequestException(String.format(ILLEGAL_VALUE_FOR_PROPERTY, propertyName)));
    }

    private static void validateNotNullIfPresent(ObjectNode input, String fieldName) throws BadRequestException {
        if (input.has(fieldName) && input.get(fieldName).isNull()) {
            throw new BadRequestException(String.format(FIELD_CAN_NOT_BE_ERASED, fieldName));
        }
    }
}
