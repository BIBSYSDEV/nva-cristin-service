package no.unit.nva.cristin.person.employment.create;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.common.ErrorMessages.invalidFieldParameterMessage;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.exceptions.BadRequestException;

public class CreatePersonEmploymentValidator {

    /**
     * Validate according to rules of upstream json schema.
     */
    public static void validate(Employment input) throws BadRequestException {
        validateNotNull(input);
        validatePositionCode(input);
        validateAffiliation(input);
    }

    private static void validateNotNull(Employment input) throws BadRequestException {
        if (isNull(input)) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD);
        }
    }

    private static void validatePositionCode(Employment input) throws BadRequestException {
        var code = Employment.extractPositionCodeFromTypeUri(input.getType());
        if (code.isEmpty() || code.get().isBlank()) {
            throw new BadRequestException(invalidFieldParameterMessage(TYPE));
        }
    }

    private static void validateAffiliation(Employment input) throws BadRequestException {
        var orgId = UriUtils.extractLastPathElement(input.getOrganization());
        if (isNull(CristinOrganization.fromIdentifier(orgId))) {
            throw new BadRequestException(invalidFieldParameterMessage(ORGANIZATION));
        }
    }
}
