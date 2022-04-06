package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.net.MediaType;
import no.unit.nva.exception.UnauthorizedException;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.List;

import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;

public class UpdateCristinProjectHandler extends ApiGatewayHandler<String, Void> {

    public static final String ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD =
            "No supported fields in payload, not doing anything";


    @SuppressWarnings("unused")
    @JacocoGenerated
    public UpdateCristinProjectHandler() {
        super(String.class, new Environment());
    }

    @Override
    protected Void processInput(String input, RequestInfo requestInfo, Context context) throws ApiGatewayException {

        validateHasAccessRights(requestInfo);
        throw new BadRequestException(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD);
    }

    @Override
    protected Integer getSuccessStatusCode(String input, Void output) {
        return HttpURLConnection.HTTP_NO_CONTENT;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    private void validateHasAccessRights(RequestInfo requestInfo) throws ForbiddenException, UnauthorizedException {
        if (!AccessUtils.requesterIsUserAdministrator(requestInfo)) {
            throw new ForbiddenException();
        }
    }
}
