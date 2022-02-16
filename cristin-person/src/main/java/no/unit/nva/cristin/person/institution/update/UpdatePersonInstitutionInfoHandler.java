package no.unit.nva.cristin.person.institution.update;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.person.institution.common.PersonInstitutionInfoHandler;
import no.unit.nva.cristin.person.model.nva.PersonInstInfoPatch;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class UpdatePersonInstitutionInfoHandler extends PersonInstitutionInfoHandler<PersonInstInfoPatch, String> {

    private final transient UpdatePersonInstInfoClient apiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public UpdatePersonInstitutionInfoHandler() {
        this(new UpdatePersonInstInfoClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    @JacocoGenerated
    public UpdatePersonInstitutionInfoHandler(UpdatePersonInstInfoClient apiClient, Environment environment) {
        super(PersonInstInfoPatch.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected String processInput(PersonInstInfoPatch input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        AccessUtils.validateIdentificationNumberAccess(requestInfo);

        validateNotNull(input);
        validateQueryParameters(requestInfo);
        String personId = getValidPersonId(requestInfo);
        String orgId = getValidOrgId(requestInfo);

        return apiClient.updatePersonInstitutionInfoInCristin(personId, orgId, input);
    }

    private void validateNotNull(PersonInstInfoPatch input) throws BadRequestException {
        if (isNull(input)) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(PersonInstInfoPatch input, String output) {
        return HttpURLConnection.HTTP_NO_CONTENT;
    }
}
