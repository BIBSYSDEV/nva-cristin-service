package no.unit.nva.cristin.person.update;

import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.unit.nva.cristin.common.Utils;
import nva.commons.apigateway.exceptions.BadRequestException;

public class PersonPatchValidator {

    public static final String ORCID_IS_NOT_VALID = "ORCID is not valid";
    public static final String FIELD_CAN_NOT_BE_ERASED = "Field %s can not be erased";
    public static final String RESERVED_FIELD_CAN_ONLY_BE_SET_TO_TRUE =
        "Reserved field can only be set to true if present";

    /**
     * Validate according to rules of upstream json schema.
     */
    public static void validate(ObjectNode input) throws BadRequestException {
        validateOrcidIfPresent(input);
        validateFirstNameIfPresent(input);
        validateLastNameIfPresent(input);
        validateReservedIfPresent(input);
    }

    private static void validateOrcidIfPresent(ObjectNode input) throws BadRequestException {
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
}
