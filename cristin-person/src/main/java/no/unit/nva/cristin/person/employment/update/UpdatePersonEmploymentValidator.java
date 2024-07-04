package no.unit.nva.cristin.person.employment.update;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.common.ErrorMessages.invalidFieldParameterMessage;
import static no.unit.nva.cristin.model.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.FULL_TIME_PERCENTAGE;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.time.Instant;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.exceptions.BadRequestException;

public class UpdatePersonEmploymentValidator {

    /**
     * Validate according to rules of upstream json schema.
     */
    public static void validate(ObjectNode input) throws BadRequestException {
        validateNotNull(input);
        validatePositionCode(input);
        validateAffiliation(input);
        validateStartDate(input);
        validateDate(input, END_DATE);
        validateFullTimePercentage(input);
    }

    private static void validateNotNull(ObjectNode input) throws BadRequestException {
        if (isNull(input)) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD);
        }
    }

    private static void validatePositionCode(ObjectNode input) throws BadRequestException {
        if (input.has(TYPE)) {
            var type = parseUriField(input, TYPE);
            var code = Employment.extractPositionCodeFromTypeUri(type);
            if (code.isEmpty() || code.get().isBlank()) {
                throw new BadRequestException(invalidFieldParameterMessage(TYPE));
            }
        }
    }

    private static void validateAffiliation(ObjectNode input) throws BadRequestException {
        if (input.has(ORGANIZATION)) {
            var organization = parseUriField(input, ORGANIZATION);
            var orgId = UriUtils.extractLastPathElement(organization);
            if (isNull(CristinOrganization.fromIdentifier(orgId))) {
                throw new BadRequestException(invalidFieldParameterMessage(ORGANIZATION));
            }
        }
    }

    private static URI parseUriField(ObjectNode input, String fieldName) throws BadRequestException {
        return attempt(() -> new URI(input.get(fieldName).asText()))
            .orElseThrow(fail -> new BadRequestException(invalidFieldParameterMessage(fieldName)));
    }

    private static void validateStartDate(ObjectNode input) throws BadRequestException {
        if (input.has(START_DATE) && input.get(START_DATE).isNull()) {
            throw new BadRequestException(invalidFieldParameterMessage(START_DATE));
        }
        validateDate(input, START_DATE);
    }

    private static void validateDate(ObjectNode input, String fieldName) throws BadRequestException {
        if (input.has(fieldName) && !input.get(fieldName).isNull()) {
            attempt(() -> Instant.parse(input.get(fieldName).asText()))
                .orElseThrow(fail -> new BadRequestException(invalidFieldParameterMessage(fieldName)));
        }
    }

    private static void validateFullTimePercentage(ObjectNode input) throws BadRequestException {
        if (input.has(FULL_TIME_PERCENTAGE) && !input.get(FULL_TIME_PERCENTAGE).isNull()) {
            attempt(() -> Double.parseDouble(input.get(FULL_TIME_PERCENTAGE).asText()))
                .orElseThrow(fail -> new BadRequestException(invalidFieldParameterMessage(FULL_TIME_PERCENTAGE)));
        }
    }
}
