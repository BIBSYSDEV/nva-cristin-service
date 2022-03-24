package no.unit.nva.cristin.person;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PERSON_ID;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import static nva.commons.core.attempt.Try.attempt;
import no.unit.nva.cristin.common.Utils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;

public class HandlerUtil {

    /**
     * Gets a valid person identifier from request path parameter or throws exception.
     */
    public static String getValidPersonId(RequestInfo requestInfo) throws BadRequestException {
        String identifier = attempt(() -> requestInfo.getPathParameter(PERSON_ID)).orElse(fail -> EMPTY_STRING);
        if (!Utils.isPositiveInteger(identifier)) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PERSON_ID);
        }
        return identifier;
    }
}
