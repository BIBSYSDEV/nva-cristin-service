package no.unit.nva.cristin.person.update;

import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import no.unit.nva.cristin.common.Utils;
import nva.commons.apigateway.exceptions.BadRequestException;
import org.json.JSONObject;

public class PersonPatchValidator {

    public static final String ORCID_IS_NOT_VALID = "ORCID is not valid";
    public static final String FIELD_CAN_NOT_BE_ERASED = "Field %s can not be erased";
    public static final String RESERVED_FIELD_CAN_ONLY_BE_SET_TO_TRUE =
        "Reserved field can only be set to true if present";

    // Validate according to rules of upstream json schema
    public static void validate(JSONObject input) throws BadRequestException {
        validateOrcidIfPresent(input);
        validateFirstNameIfPresent(input);
        validateLastNameIfPresent(input);
        validateReservedIfPresent(input);
    }

    private static void validateOrcidIfPresent(JSONObject input) throws BadRequestException {
        if (!input.isNull(ORCID) && !Utils.isOrcid(String.valueOf(input.get(ORCID)))) {
            throw new BadRequestException(ORCID_IS_NOT_VALID);
        }
    }

    private static void validateFirstNameIfPresent(JSONObject input) throws BadRequestException {
        validateNotNullIfPresent(input, FIRST_NAME);
    }

    private static void validateLastNameIfPresent(JSONObject input) throws BadRequestException {
        validateNotNullIfPresent(input, LAST_NAME);
    }

    private static void validateNotNullIfPresent(JSONObject input, String fieldName) throws BadRequestException {
        if (input.has(fieldName) && input.isNull(fieldName)) {
            throw new BadRequestException(String.format(FIELD_CAN_NOT_BE_ERASED, fieldName));
        }
    }

    private static void validateReservedIfPresent(JSONObject input) throws BadRequestException {
        if (input.has(RESERVED) && !input.optBoolean(RESERVED)) {
            throw new BadRequestException(RESERVED_FIELD_CAN_ONLY_BE_SET_TO_TRUE);
        }
    }
}
