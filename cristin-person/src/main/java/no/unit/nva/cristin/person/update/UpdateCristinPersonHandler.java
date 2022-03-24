package no.unit.nva.cristin.person.update;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.person.HandlerUtil.getValidPersonId;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.MediaType;
import java.net.HttpURLConnection;
import java.util.List;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.exception.UnauthorizedException;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class UpdateCristinPersonHandler extends ApiGatewayHandler<String, Void> {

    public static final String ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD =
        "No supported fields in payload, not doing anything";

    private final transient UpdateCristinPersonApiClient apiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public UpdateCristinPersonHandler() {
        this(new UpdateCristinPersonApiClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    public UpdateCristinPersonHandler(UpdateCristinPersonApiClient apiClient, Environment environment) {
        super(String.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Void processInput(String input, RequestInfo requestInfo, Context context) throws ApiGatewayException {

        validateHasAccessRights(requestInfo);

        ObjectNode objectNode = readJsonFromInput(input);
        PersonPatchValidator.validate(objectNode);
        ObjectNode cristinJson = new CristinPersonPatchJsonCreator(objectNode).create().getOutput();

        if (noSupportedValuesPresent(cristinJson)) {
            throw new BadRequestException(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD);
        }

        String personId = getValidPersonId(requestInfo);

        return apiClient.updatePersonInCristin(personId, cristinJson);
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

    private ObjectNode readJsonFromInput(String input) throws BadRequestException {
        return attempt(() -> (ObjectNode) OBJECT_MAPPER.readTree(input))
            .orElseThrow(fail -> new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD));
    }

    private boolean noSupportedValuesPresent(ObjectNode cristinJson) {
        return cristinJson.isEmpty();
    }
}
