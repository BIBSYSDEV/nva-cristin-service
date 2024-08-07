package no.unit.nva.cristin.person.employment.create;

import static no.unit.nva.cristin.common.Utils.CAN_UPDATE_ANY_INSTITUTION;
import static no.unit.nva.cristin.common.Utils.extractCristinInstitutionIdentifier;
import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;
import static nva.commons.apigateway.AccessRight.MANAGE_CUSTOMERS;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.net.HttpURLConnection;
import java.util.List;
import no.unit.nva.common.IdCreatedLogger;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreatePersonEmploymentHandler extends ApiGatewayHandler<Employment, Employment> {

    private static final Logger logger = LoggerFactory.getLogger(CreatePersonEmploymentHandler.class);

    private final transient CreatePersonEmploymentClient apiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public CreatePersonEmploymentHandler() {
        this(new CreatePersonEmploymentClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    @JacocoGenerated
    public CreatePersonEmploymentHandler(CreatePersonEmploymentClient apiClient, Environment environment) {
        super(Employment.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Employment processInput(Employment input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        var identifier = getValidPersonId(requestInfo);
        var institutionIdentifier = fullAccessValueOrInstitutionNumber(requestInfo);

        return apiClient.createEmploymentInCristin(identifier, input, institutionIdentifier);
    }

    @Override
    protected Integer getSuccessStatusCode(Employment input, Employment output) {
        new IdCreatedLogger().logId(output);

        return HttpURLConnection.HTTP_CREATED;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    @Override
    protected void validateRequest(Employment input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        validateHasAccessRights(requestInfo);
        logUser(requestInfo);
        CreatePersonEmploymentValidator.validate(input);
    }

    private void validateHasAccessRights(RequestInfo requestInfo) throws ForbiddenException {
        if (!AccessUtils.requesterIsUserAdministrator(requestInfo) && !requestInfo.userIsAuthorized(MANAGE_CUSTOMERS)) {
            throw new ForbiddenException();
        }
    }

    private void logUser(RequestInfo requestInfo) {
        logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));
    }

    private String fullAccessValueOrInstitutionNumber(RequestInfo requestInfo)
        throws BadRequestException, ForbiddenException {

        return requestInfo.userIsAuthorized(MANAGE_CUSTOMERS)
                   ? CAN_UPDATE_ANY_INSTITUTION : extractCristinInstitutionIdentifier(requestInfo);
    }
}
