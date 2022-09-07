package no.unit.nva.cristin.person.employment.update;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD;
import static no.unit.nva.cristin.common.Utils.getValidEmploymentId;
import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import static no.unit.nva.cristin.common.Utils.readJsonFromInput;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

public class UpdatePersonEmploymentHandler extends ApiGatewayHandler<String, Void> {

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

        validateHasAccessRights(requestInfo);

        ObjectNode objectNode = readJsonFromInput(input);
        UpdatePersonEmploymentValidator.validate(objectNode);
        ObjectNode cristinJson = new UpdateCristinEmploymentJsonCreator(objectNode).create().getOutput();

        if (cristinJson.isEmpty()) {
            throw new BadRequestException(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD);
        }

        String personId = getValidPersonId(requestInfo);
        String employmentId = getValidEmploymentId(requestInfo);

        return apiClient.updatePersonEmploymentInCristin(personId, employmentId, cristinJson);
    }

    @Override
    protected Integer getSuccessStatusCode(String input, Void output) {
        return HttpURLConnection.HTTP_NO_CONTENT;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    private void validateHasAccessRights(RequestInfo requestInfo) throws ForbiddenException {
        if (!AccessUtils.requesterIsUserAdministrator(requestInfo)) {
            throw new ForbiddenException();
        }
    }

}
