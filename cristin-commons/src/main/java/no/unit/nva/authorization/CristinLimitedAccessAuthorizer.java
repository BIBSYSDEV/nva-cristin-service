package no.unit.nva.authorization;

import no.unit.commons.apigateway.authentication.RequestAuthorizer;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;

public class CristinLimitedAccessAuthorizer extends RequestAuthorizer {

    public CristinLimitedAccessAuthorizer(Environment environment) {
        super(environment);
    }

    @Override
    protected String principalId() throws ForbiddenException {
        return "AccessNationalIdentificationNumber";
    }

    @Override
    protected String fetchSecret() throws ForbiddenException {
        return "AccessNationalIdentificationNumberSecret";
    }
}
