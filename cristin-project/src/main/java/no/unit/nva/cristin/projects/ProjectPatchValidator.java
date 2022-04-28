package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.utils.PatchValidator;
import nva.commons.apigateway.exceptions.BadRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTRIBUTORS;
import static no.unit.nva.cristin.model.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TITLE;
import static nva.commons.core.attempt.Try.attempt;


public class ProjectPatchValidator extends PatchValidator {

    private static final Set<String> SUPPORTED_PATCH_FIELDS =
            Set.of(TITLE, CONTRIBUTORS, COORDINATING_INSTITUTION, LANGUAGE, START_DATE, END_DATE);
    public static final String UNSUPPORTED_FIELDS_IN_PAYLOAD = "Unsupported fields in payload %s";


    /**
     * Validate changes to Project, both nullable fields and values.
     *
     * @param input ObjectNode containing fields with input data to be changed.
     * @throws BadRequestException thrown when input has illegal or invalid values.
     */
    public static void validate(ObjectNode input) throws BadRequestException {
        validateExtraPayload(input);
        validateContributors(input);
        validateCoordinatingInstitution(input);
        validateInstantIfPresent(input, END_DATE);
        validateInstantIfPresent(input, START_DATE);
        validateLanguage(input);
    }

    private static void validateExtraPayload(ObjectNode input) throws BadRequestException {
        List<String> keys = new ArrayList<>();
        input.fieldNames().forEachRemaining(field -> {
            if (!SUPPORTED_PATCH_FIELDS.contains(field)) {
                keys.add(field);
            }
        });
        if (!keys.isEmpty()) {
            throw new BadRequestException(String.format(UNSUPPORTED_FIELDS_IN_PAYLOAD, keys));
        }
    }

    private static void validateCoordinatingInstitution(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, COORDINATING_INSTITUTION);
    }

    private static void validateContributors(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, CONTRIBUTORS);
        if (propertyHasValue(input, CONTRIBUTORS)) {
            TypeReference<List<NvaContributor>> typeRef = new TypeReference<>() {
            };
            validateJsonReadable(typeRef, input.get(CONTRIBUTORS).asText(), CONTRIBUTORS);
        }
    }

    private static void validateJsonReadable(TypeReference<?> typeRef, String content, String propertyName)
            throws BadRequestException {
        attempt(() -> OBJECT_MAPPER.readValue(content, typeRef))
                .orElseThrow(fail ->
                        new BadRequestException(String.format(ILLEGAL_VALUE_FOR_PROPERTY, propertyName)));
    }

}
