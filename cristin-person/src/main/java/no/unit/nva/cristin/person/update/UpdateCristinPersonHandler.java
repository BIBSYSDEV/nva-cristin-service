package no.unit.nva.cristin.person.update;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.MediaType;
import java.util.Objects;
import java.util.Optional;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.utils.AccessUtils;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.apigateway.exceptions.UnauthorizedException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.List;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD;
import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static nva.commons.core.attempt.Try.attempt;

public class UpdateCristinPersonHandler extends ApiGatewayHandler<String, Void> {

    public static final String ERROR_MESSAGE_IDENTIFIERS_DO_NOT_MATCH = "Identifier from path does not match "
                                                                        + "identifier from user info";
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
        String personId = getValidPersonId(requestInfo);

        if (clientCanUpdateAllFields(requestInfo)) {
            PersonPatchValidator.validate(objectNode);
            ObjectNode cristinJson = new CristinPersonPatchJsonCreator(objectNode).create().getOutput();
            checkHasFields(cristinJson);

            return apiClient.updatePersonInCristin(personId, cristinJson);
        } else if (clientCanUpdateOwnData(requestInfo)) {
            String personIdFromCognito = parseLastPartOfPersonCristinIdFromCognito(requestInfo).orElseThrow();
            checkIdentifiersMatch(personId, personIdFromCognito);
            PersonPatchValidator.validateOrcidIfPresent(objectNode);
            ObjectNode cristinJson =
                new CristinPersonPatchJsonCreator(objectNode).createWithAllowedUserModifiableData().getOutput();
            checkHasFields(cristinJson);

            return apiClient.updatePersonInCristin(personId, cristinJson);
        } else {
            throw new ForbiddenException();
        }
    }

    private void checkHasFields(ObjectNode cristinJson) throws BadRequestException {
        if (cristinJson.isEmpty()) {
            throw new BadRequestException(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD);
        }
    }

    private void checkIdentifiersMatch(String personId, String personIdFromCognito) throws BadRequestException {
        if (!Objects.equals(personId, personIdFromCognito)) {
            throw new BadRequestException(ERROR_MESSAGE_IDENTIFIERS_DO_NOT_MATCH);
        }
    }

    private boolean clientCanUpdateOwnData(RequestInfo requestInfo) throws UnauthorizedException {
        return parseLastPartOfPersonCristinIdFromCognito(requestInfo).isPresent();
    }

    private Optional<String> parseLastPartOfPersonCristinIdFromCognito(RequestInfo requestInfo)
        throws UnauthorizedException {
        return Optional.ofNullable(requestInfo.getPersonCristinId()).map(UriUtils::extractLastPathElement);
    }

    private boolean clientCanUpdateAllFields(RequestInfo requestInfo) {
        return AccessUtils.requesterIsUserAdministrator(requestInfo);
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
        if (!AccessUtils.requesterIsUserAdministrator(requestInfo) && !clientCanUpdateOwnData(requestInfo)) {
            throw new ForbiddenException();
        }
    }

    private ObjectNode readJsonFromInput(String input) throws BadRequestException {
        return attempt(() -> (ObjectNode) OBJECT_MAPPER.readTree(input))
            .orElseThrow(fail -> new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD));
    }

}
