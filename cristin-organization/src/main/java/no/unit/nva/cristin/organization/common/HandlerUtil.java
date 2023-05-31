package no.unit.nva.cristin.organization.common;

import static no.unit.nva.cristin.model.Constants.FULL;
import static no.unit.nva.cristin.model.Constants.NONE;
import static no.unit.nva.cristin.model.Constants.TOP;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import java.util.Set;
import no.unit.nva.cristin.common.ErrorMessages;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;

public class HandlerUtil {

    /**
     * Gets depth parameter from the request info.
     *
     * @param requestInfo Info from the request
     */
    public static String getValidDepth(RequestInfo requestInfo) throws BadRequestException {
        if (isValidDepth(requestInfo)) {
            return requestInfo.getQueryParameters().containsKey(DEPTH)
                       ? requestInfo.getQueryParameter(DEPTH)
                       : TOP;
        } else {
            throw new BadRequestException(ErrorMessages.ERROR_MESSAGE_DEPTH_INVALID);
        }
    }

    private static boolean isValidDepth(RequestInfo requestInfo) throws BadRequestException {
        return !requestInfo.getQueryParameters().containsKey(DEPTH)
               || requestInfo.getQueryParameters().containsKey(DEPTH)
                  && Set.of(TOP, FULL, NONE).contains(requestInfo.getQueryParameter(DEPTH));
    }

}
