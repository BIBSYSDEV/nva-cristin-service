package no.unit.nva.cristin.person.update;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.EMPLOYMENTS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.person.model.nva.Employment;
import nva.commons.apigateway.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersonPatchValidator {

    private static final Logger logger = LoggerFactory.getLogger(PersonPatchValidator.class);

    public static final String ORCID_IS_NOT_VALID = "ORCID is not valid";
    public static final String FIELD_CAN_NOT_BE_ERASED = "Field %s can not be erased";
    public static final String RESERVED_FIELD_CAN_ONLY_BE_SET_TO_TRUE =
        "Reserved field can only be set to true if present";
    public static final String COULD_NOT_PARSE_EMPLOYMENT_FIELD = "Could not parse employment field because of "
                                                                  + "invalid data";
    private static final String EXCEPTION_WHEN_VALIDATING_EMPLOYMENTS = "Exception when validating employments: ";

    /**
     * Validate according to rules of upstream json schema.
     */
    public static void validate(ObjectNode input) throws BadRequestException {
        validateOrcidIfPresent(input);
        validateFirstNameIfPresent(input);
        validateLastNameIfPresent(input);
        validateReservedIfPresent(input);
        validateEmploymentsIfPresent(input);
    }

    protected static void validateOrcidIfPresent(ObjectNode input) throws BadRequestException {
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
        if (input.has(RESERVED) && !input.get(RESERVED).asBoolean()) {
            throw new BadRequestException(RESERVED_FIELD_CAN_ONLY_BE_SET_TO_TRUE);
        }
    }

    private static void validateEmploymentsIfPresent(ObjectNode input) throws BadRequestException {
        if (input.has(EMPLOYMENTS) && !input.get(EMPLOYMENTS).isNull()) {
            attempt(() -> asList(OBJECT_MAPPER.readValue(input.get(EMPLOYMENTS).asText(), Employment[].class)))
                .orElseThrow(fail -> {
                    logger.warn(EXCEPTION_WHEN_VALIDATING_EMPLOYMENTS, fail.getException());
                    return new BadRequestException(COULD_NOT_PARSE_EMPLOYMENT_FIELD);
                });
        }
    }
}
