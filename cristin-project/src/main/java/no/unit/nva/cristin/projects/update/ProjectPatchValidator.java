package no.unit.nva.cristin.projects.update;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.unit.nva.Validator;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.PatchValidator;
import nva.commons.apigateway.exceptions.BadRequestException;

import java.util.List;

import static java.lang.String.format;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTRIBUTORS;
import static no.unit.nva.cristin.model.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.FUNDING;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TITLE;
import static no.unit.nva.cristin.projects.model.nva.Funding.CODE;
import static no.unit.nva.cristin.projects.model.nva.Funding.SOURCE;
import static nva.commons.core.attempt.Try.attempt;


public class ProjectPatchValidator extends PatchValidator implements Validator<ObjectNode> {

    public static final String TITLE_MUST_HAVE_A_LANGUAGE = "Title must have a language associated";
    public static final String FUNDING_MISSING_REQUIRED_FIELDS = "Funding missing required fields";
    public static final String MUST_BE_A_LIST = "Field %s must be a list";

    /**
     * Validate changes to Project, both nullable fields and values.
     *
     * @param input ObjectNode containing fields with input data to be changed.
     * @throws BadRequestException thrown when input has illegal or invalid values.
     */
    @Override
    public void validate(ObjectNode input) throws BadRequestException {
        validateTitleAndLanguage(input);
        validateContributors(input);
        validateCoordinatingInstitution(input);
        validateInstantIfPresent(input, END_DATE);
        validateInstantIfPresent(input, START_DATE);
        validateLanguage(input);
        validateFundingsIfPresent(input);
    }

    private static void validateTitleAndLanguage(ObjectNode input) throws BadRequestException {
        // Language must have value if present, title can be 'nulled'
        validateNotNullIfPresent(input, LANGUAGE);
        if (propertyHasValue(input, TITLE) && !propertyHasValue(input, LANGUAGE)) {
            throw new BadRequestException(TITLE_MUST_HAVE_A_LANGUAGE);
        }
    }

    private static void validateCoordinatingInstitution(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, COORDINATING_INSTITUTION);
        if (propertyHasValue(input, COORDINATING_INSTITUTION)) {
            attempt(() -> OBJECT_MAPPER.readValue(input.get(COORDINATING_INSTITUTION).toString(), Organization.class))
                    .orElseThrow(fail ->
                        new BadRequestException(format(ILLEGAL_VALUE_FOR_PROPERTY, COORDINATING_INSTITUTION)));
        }

    }

    private static void validateContributors(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, CONTRIBUTORS);
        if (propertyHasValue(input, CONTRIBUTORS)) {
            TypeReference<List<NvaContributor>> typeRef = new TypeReference<>() {
            };
            validateJsonReadable(typeRef, input.get(CONTRIBUTORS).toString(), CONTRIBUTORS);
        }
    }

    private static void validateJsonReadable(TypeReference<?> typeRef, String content, String propertyName)
            throws BadRequestException {
        attempt(() -> OBJECT_MAPPER.readValue(content, typeRef))
                .orElseThrow(fail ->
                        new BadRequestException(format(ILLEGAL_VALUE_FOR_PROPERTY, propertyName)));
    }

    private static void validateFundingsIfPresent(ObjectNode input) throws BadRequestException {
        if (!input.has(FUNDING) || input.get(FUNDING).isNull()) {
            return;
        }
        if (input.get(FUNDING).isArray()) {
            var fundingArray = (ArrayNode) input.get(FUNDING);
            for (JsonNode funding : fundingArray) {
                validateFunding(funding);
            }
        } else {
            throw new BadRequestException(format(MUST_BE_A_LIST, FUNDING));
        }
    }

    private static void validateFunding(JsonNode funding) throws BadRequestException {
        if (!funding.has(SOURCE) || funding.get(SOURCE).isNull()
            || !funding.get(SOURCE).has(CODE) || funding.get(SOURCE).get(CODE).isNull()) {
            throw new BadRequestException(FUNDING_MISSING_REQUIRED_FIELDS);
        }
    }
}
