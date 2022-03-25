package no.unit.nva.cristin.person.institution.update;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.person.institution.common.PersonInstitutionInfoHandler;
import no.unit.nva.cristin.person.model.nva.PersonInstInfoPatch;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.common.Utils.getValidOrgId;
import static no.unit.nva.cristin.common.Utils.getValidPersonId;

public class UpdatePersonInstitutionInfoHandler extends PersonInstitutionInfoHandler<PersonInstInfoPatch, String> {

    public static final String ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD =
        "No supported fields in payload, not doing anything";

    private final transient UpdatePersonInstitutionInfoClient apiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public UpdatePersonInstitutionInfoHandler() {
        this(new UpdatePersonInstitutionInfoClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    @JacocoGenerated
    public UpdatePersonInstitutionInfoHandler(UpdatePersonInstitutionInfoClient apiClient, Environment environment) {
        super(PersonInstInfoPatch.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected String processInput(PersonInstInfoPatch input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        AccessUtils.validateIdentificationNumberAccess(requestInfo);

        validateNotEmpty(input);
        validateQueryParameters(requestInfo);
        String personId = getValidPersonId(requestInfo);
        String orgId = getValidOrgId(requestInfo);

        return apiClient.updatePersonInstitutionInfoInCristin(personId, orgId, input);
    }

    private void validateNotEmpty(PersonInstInfoPatch input) throws BadRequestException {
        if (isNull(input) || noSupportedValuesPresent(input)) {
            throw new BadRequestException(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD);
        }
    }

    private boolean noSupportedValuesPresent(PersonInstInfoPatch input) {
        return isNull(input.getPhone()) && isNull(input.getEmail());
    }

    @Override
    protected Integer getSuccessStatusCode(PersonInstInfoPatch input, String output) {
        return HttpURLConnection.HTTP_NO_CONTENT;
    }
}
