package no.unit.nva.cristin.person.employment.update;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD;
import static no.unit.nva.cristin.common.Utils.extractCristinInstitutionIdentifier;
import static no.unit.nva.cristin.common.Utils.getValidEmploymentId;
import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import static no.unit.nva.cristin.common.Utils.readJsonFromInput;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import java.net.HttpURLConnection;
import java.util.List;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdatePersonEmploymentHandler extends ApiGatewayHandler<String, Void> {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePersonEmploymentHandler.class);

    private final transient UpdatePersonEmploymentClient apiClient;

    @SuppressWarnings("unused")
    public UpdatePersonEmploymentHandler() {
        this(new UpdatePersonEmploymentClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    public UpdatePersonEmploymentHandler(UpdatePersonEmploymentClient apiClient, Environment environment) {
        super(String.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Void processInput(String input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        var objectNode = readJsonFromInput(input);
        UpdatePersonEmploymentValidator.validate(objectNode);
        var cristinJson = new UpdateCristinEmploymentJsonCreator(objectNode).create().getOutput();

        if (cristinJson.isEmpty()) {
            throw new BadRequestException(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD);
        }

        var personId = getValidPersonId(requestInfo);
        var employmentId = getValidEmploymentId(requestInfo);

        logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));

        return apiClient.updatePersonEmploymentInCristin(personId, employmentId, cristinJson,
                                                         extractCristinInstitutionIdentifier(requestInfo));
    }

    @Override
    protected Integer getSuccessStatusCode(String input, Void output) {
        return HttpURLConnection.HTTP_NO_CONTENT;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    @Override
    protected void validateRequest(String input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateHasAccessRights(requestInfo);
    }

    private void validateHasAccessRights(RequestInfo requestInfo) throws ForbiddenException {
        if (!AccessUtils.requesterIsUserAdministrator(requestInfo)) {
            throw new ForbiddenException();
        }
    }

}
