package no.unit.nva.cristin.person.update;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.BACKGROUND;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.EMPLOYMENTS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.KEYWORDS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static no.unit.nva.validation.PatchValidator.validateDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.person.employment.create.CreatePersonEmploymentValidator;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PersonPatchValidator {

    private static final Logger logger = LoggerFactory.getLogger(PersonPatchValidator.class);

    public static final String ORCID_IS_NOT_VALID = "ORCID is not valid";
    public static final String FIELD_CAN_NOT_BE_ERASED = "Field %s can not be erased";
    public static final String RESERVED_MUST_BE_BOOLEAN =
        "Reserved field can only be set to boolean value";
    public static final String COULD_NOT_PARSE_EMPLOYMENT_FIELD = "Could not parse employment field because of "
                                                                  + "invalid data";
    private static final String EXCEPTION_WHEN_VALIDATING_EMPLOYMENTS = "Exception when validating employments: {}";
    private static final String EXCEPTION_WHEN_VALIDATING_KEYWORDS = "Exception when validating keywords: {}";
    public static final String COULD_NOT_PARSE_KEYWORD_FIELD = "Could not parse keyword field because of "
                                                                  + "invalid data";

    @JacocoGenerated
    private PersonPatchValidator() {
        // NO-OP
    }

    /**
     * Validate according to rules of upstream json schema.
     */
    public static void validate(ObjectNode input) throws BadRequestException {
        validateOrcidIfPresent(input);
        validateFirstNameIfPresent(input);
        validateLastNameIfPresent(input);
        validateReservedIfPresent(input);
        validateEmploymentsIfPresent(input);
        validateKeywordsIfPresent(input);
        validateDescription(input, BACKGROUND);
    }

    /**
     * Validate an orcid if present in input node.
     */
    public static void validateOrcidIfPresent(ObjectNode input) throws BadRequestException {
        if (input.has(ORCID) && !input.get(ORCID).isNull() && !Utils.isOrcid(input.get(ORCID).asText())) {
            throw new BadRequestException(ORCID_IS_NOT_VALID);
        }
    }

    private static void validateFirstNameIfPresent(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, FIRST_NAME);
    }

    private static void validateLastNameIfPresent(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, LAST_NAME);
    }

    private static void validateNotNullIfPresent(ObjectNode input, String fieldName) throws BadRequestException {
        if (input.has(fieldName) && input.get(fieldName).isNull()) {
            throw new BadRequestException(String.format(FIELD_CAN_NOT_BE_ERASED, fieldName));
        }
    }

    private static void validateReservedIfPresent(ObjectNode input) throws BadRequestException {
        if (input.has(RESERVED) && !input.get(RESERVED).isBoolean()) {
            throw new BadRequestException(RESERVED_MUST_BE_BOOLEAN);
        }
    }

    private static void validateEmploymentsIfPresent(ObjectNode input) throws BadRequestException {
        if (input.has(EMPLOYMENTS) && !input.get(EMPLOYMENTS).isNull()) {
            var employments = parseEmployments(input);
            for (Employment employment : employments) {
                CreatePersonEmploymentValidator.validate(employment);
            }
        }
    }

    private static Employment[] parseEmployments(ObjectNode input) throws BadRequestException {
        try {
            return OBJECT_MAPPER.readValue(input.get(EMPLOYMENTS).toString(), Employment[].class);
        } catch (JsonProcessingException e) {
            logger.warn(EXCEPTION_WHEN_VALIDATING_EMPLOYMENTS, e.getMessage());
            throw new BadRequestException(COULD_NOT_PARSE_EMPLOYMENT_FIELD);
        }
    }

    private static void validateKeywordsIfPresent(ObjectNode input) throws BadRequestException {
        if (input.has(KEYWORDS) && !input.get(KEYWORDS).isNull()) {
            parseKeywords(input);
        }
    }

    private static void parseKeywords(ObjectNode input) throws BadRequestException {
        try {
            OBJECT_MAPPER.readValue(input.get(KEYWORDS).toString(), TypedValue[].class);
        } catch (JsonProcessingException e) {
            logger.warn(EXCEPTION_WHEN_VALIDATING_KEYWORDS, e.getMessage());
            throw new BadRequestException(COULD_NOT_PARSE_KEYWORD_FIELD);
        }
    }
}
