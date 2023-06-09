package no.unit.nva.cristin.organization.common;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_DEPTH_INVALID;
import static no.unit.nva.cristin.model.Constants.FULL;
import static no.unit.nva.cristin.model.Constants.NONE;
import static no.unit.nva.cristin.model.Constants.TOP;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import java.util.Set;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;

public class HandlerUtil {

    /**
     * Gets depth parameter from the request info.
     *
     * @param requestInfo Info from the request
     */
    public static String getValidDepth(RequestInfo requestInfo) throws BadRequestException {
        var depth = requestInfo.getQueryParameterOpt(DEPTH).orElse(TOP);
        validateDepth(depth);
        return depth;
    }

    private static void validateDepth(String depth) throws BadRequestException {
        if (!Set.of(TOP, FULL, NONE).contains(depth)) {
            throw new BadRequestException(ERROR_MESSAGE_DEPTH_INVALID);
        }
    }

}
