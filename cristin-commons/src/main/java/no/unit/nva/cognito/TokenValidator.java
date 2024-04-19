package no.unit.nva.cognito;

import static nva.commons.core.attempt.Try.attempt;
import nva.commons.apigateway.RequestInfo;

public class TokenValidator {

    private final RequestInfo requestInfo;

    public TokenValidator(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public boolean clientHasToken(RequestInfo requestInfo) {
        var token = attempt(requestInfo::getAuthHeader).toOptional();
        return token.isPresent();
    }

    /**
     * Missing access rights might mean that client either has no access rights, or that Cognito is not responding on
     * our request.
     */
    public boolean hasTokenButNoAccessRights() {
        return clientHasToken(requestInfo) && requestInfo.getAccessRights().isEmpty();
    }

}
