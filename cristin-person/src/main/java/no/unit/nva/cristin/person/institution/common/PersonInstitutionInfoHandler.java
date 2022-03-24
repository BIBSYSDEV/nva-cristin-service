package no.unit.nva.cristin.person.institution.common;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP;
import static no.unit.nva.cristin.common.Utils.removeUnitPartFromIdentifierIfPresent;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static nva.commons.core.attempt.Try.attempt;
import com.google.common.net.MediaType;
import java.util.List;
import no.unit.nva.cristin.common.Utils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;

public abstract class PersonInstitutionInfoHandler<I, O> extends ApiGatewayHandler<I, O> {

    public static final String ORG_ID = "orgId";
    public static final String INVALID_ORGANIZATION_ID = "Invalid path parameter for organization id";

    public PersonInstitutionInfoHandler(Class<I> iclass, Environment environment) {
        super(iclass, environment);
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    protected void validateQueryParameters(RequestInfo requestInfo) throws BadRequestException {
        if (!requestInfo.getQueryParameters().keySet().isEmpty()) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP);
        }
    }

    protected String getValidOrgId(RequestInfo requestInfo) throws BadRequestException {
        String identifier = attempt(() -> requestInfo.getPathParameter(ORG_ID)).orElse(fail -> EMPTY_STRING);
        String cristinInstitutionId = removeUnitPartFromIdentifierIfPresent(identifier);
        if (isValidIdentifier(cristinInstitutionId)) {
            return cristinInstitutionId;
        }
        throw new BadRequestException(INVALID_ORGANIZATION_ID);
    }

    private boolean isValidIdentifier(String identifier) {
        return Utils.isPositiveInteger(identifier);
    }
}
