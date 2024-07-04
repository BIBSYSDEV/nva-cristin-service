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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD;
import static no.unit.nva.cristin.common.Utils.getValidOrgId;
import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;

public class UpdatePersonInstitutionInfoHandler extends PersonInstitutionInfoHandler<PersonInstInfoPatch, String> {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePersonInstitutionInfoHandler.class);

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

        logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));

        validateNotEmpty(input);
        validateQueryParameters(requestInfo);
        var personId = getValidPersonId(requestInfo);
        var orgId = getValidOrgId(requestInfo);

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
