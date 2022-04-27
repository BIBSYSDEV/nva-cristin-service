package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.MediaType;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.List;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.projects.FetchOneCristinProject.getValidIdentifier;
import static nva.commons.core.attempt.Try.attempt;

public class UpdateCristinProjectHandler extends ApiGatewayHandler<String, Void> {

    public static final String ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD =
            "No supported fields in payload, not doing anything";

    private final transient CristinApiClient cristinApiClient;

    @JacocoGenerated
    public UpdateCristinProjectHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public UpdateCristinProjectHandler(Environment environment) {
        this(new CristinApiClient(), environment);
    }

    public UpdateCristinProjectHandler(CristinApiClient cristinApiClient, Environment environment) {
        super(String.class, environment);
        this.cristinApiClient = cristinApiClient;
    }

    @Override
    protected Void processInput(String input, RequestInfo requestInfo, Context context) throws ApiGatewayException {

        AccessUtils.verifyRequesterCanEditProjects(requestInfo);

        ObjectNode objectNode = readJsonFromInput(input);
        ProjectPatchValidator.validate(objectNode);
        ObjectNode cristinJson = new CristinProjectPatchJsonCreator(objectNode).create().getOutput();

        if (noSupportedValuesPresent(cristinJson)) {
            throw new BadRequestException(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD);
        }

        return cristinApiClient.updateProjectInCristin(getValidIdentifier(requestInfo), cristinJson);
    }

    @Override
    protected Integer getSuccessStatusCode(String input, Void output) {
        return HttpURLConnection.HTTP_NO_CONTENT;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    private ObjectNode readJsonFromInput(String input) throws BadRequestException {
        return attempt(() -> (ObjectNode) OBJECT_MAPPER.readTree(input))
                .orElseThrow(fail -> new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD));
    }

    private boolean noSupportedValuesPresent(ObjectNode cristinJson) {
        return cristinJson.isEmpty();
    }

}
