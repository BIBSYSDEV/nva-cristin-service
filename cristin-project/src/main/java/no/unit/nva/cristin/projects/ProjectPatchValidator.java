package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.utils.PatchValidator;
import nva.commons.apigateway.exceptions.BadRequestException;

import java.util.List;
import java.util.Map;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.ALTERNATIVE_TITLES;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTRIBUTORS;
import static no.unit.nva.cristin.model.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.POPULAR_SCIENTIFIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TITLE;
import static nva.commons.core.attempt.Try.attempt;


public class ProjectPatchValidator extends PatchValidator {

    /**
     * Validate changes to Project, both nullable fields and values.
     * @param input ObjectNode containing fields with input data to be changed.
     * @throws BadRequestException thrown when input has illegal or invalid values.
     */
    public static void validate(ObjectNode input) throws BadRequestException {

        validateNonNullableFieldsNotNull(input);

        validateLanguageStringMap(input, ACADEMIC_SUMMARY);
        validateAlternativeTitles(input);
        validateLanguageStringMap(input, POPULAR_SCIENTIFIC_SUMMARY);
        validateInstant(input, END_DATE);
        validateInstant(input, START_DATE);
        validateLanguage(input);
        validateContributors(input);
    }

    private static void validateNonNullableFieldsNotNull(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, TITLE);
        validateNotNullIfPresent(input, START_DATE);
        validateNotNullIfPresent(input, CONTRIBUTORS);
        validateNotNullIfPresent(input, COORDINATING_INSTITUTION);
    }

    private static void validateContributors(ObjectNode input) throws BadRequestException {
        TypeReference<List<NvaContributor>> typeRef = new TypeReference<>() { };
        validateJsonReadable(typeRef, input.get(CONTRIBUTORS).asText(), CONTRIBUTORS);
    }

    private static void validateJsonReadable(TypeReference<?> typeRef, String content, String propertyName)
            throws BadRequestException {
        attempt(() -> OBJECT_MAPPER.readValue(content, typeRef))
                .orElseThrow(fail ->
                        new BadRequestException(String.format(ILLEGAL_VALUE_FOR_PROPERTY, propertyName)));
    }

    private static void validateAlternativeTitles(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input,ALTERNATIVE_TITLES);
        TypeReference<List<Map<String, String>>> typeRef = new TypeReference<>() { };
        validateJsonReadable(typeRef, input.get(ALTERNATIVE_TITLES).asText(), ALTERNATIVE_TITLES);
    }
}
