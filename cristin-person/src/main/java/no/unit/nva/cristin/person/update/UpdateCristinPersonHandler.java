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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD;
import static no.unit.nva.cristin.common.Utils.extractCristinInstitutionIdentifier;
import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import static no.unit.nva.cristin.common.Utils.readJsonFromInput;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_EMPLOYMENTS;
import static no.unit.nva.cristin.person.model.cristin.CristinPerson.PERSON_NVI;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;
import static nva.commons.core.attempt.Try.attempt;

public class UpdateCristinPersonHandler extends ApiGatewayHandler<String, Void> {

    private static final Logger logger = LoggerFactory.getLogger(UpdateCristinPersonHandler.class);

    public static final String ERROR_MESSAGE_IDENTIFIERS_DO_NOT_MATCH = "Identifier from path does not match "
                                                                        + "identifier from user info";
    private static final String PERSON_ID_NOT_FOUND = "PERSON_ID_NOT_FOUND_IN_QUERY_PARAMETERS";
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
        logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));

        var objectNode = readJsonFromInput(input);
        var personId = getValidPersonId(requestInfo);

        if (clientCanUpdateAllFields(requestInfo)) {
            PersonPatchValidator.validate(objectNode);
            objectNode = filterInput(requestInfo, objectNode);
            var cristinJson = new CristinPersonPatchJsonCreator(objectNode)
                                  .create()
                                  .getOutput();
            checkHasFields(cristinJson);

            if (cristinJson.has(CRISTIN_EMPLOYMENTS) || cristinJson.has(PERSON_NVI)) {
                return apiClient.updatePersonInCristin(personId, cristinJson,
                                                       extractCristinInstitutionIdentifier(requestInfo));
            } else {
                return apiClient.updatePersonInCristin(personId, cristinJson);
            }
        } else if (clientCanUpdateOwnData(requestInfo)) {
            var personIdFromCognito = parseLastPartOfPersonCristinIdFromCognito(requestInfo).orElseThrow();
            checkIdentifiersMatch(personId, personIdFromCognito);
            PersonPatchValidator.validateUserModifiableFields(objectNode);
            var cristinJson = new CristinPersonPatchJsonCreator(objectNode)
                                  .createWithAllowedUserModifiableData()
                                  .getOutput();
            checkHasFields(cristinJson);

            return apiClient.updatePersonInCristin(personIdFromCognito, cristinJson);
        } else {
            logFailedAuthorization(requestInfo, personId);
            throw new ForbiddenException();
        }
    }

    private static void logFailedAuthorization(RequestInfo requestInfo, String subject) throws UnauthorizedException {
        logger.error("Unauthorized attempt to update person with id: {} by logged in user id {}", subject,
                     requestInfo.getPersonCristinId());
    }

    private ObjectNode filterInput(RequestInfo requestInfo, ObjectNode objectNode) {
        var clientInstNr = attempt(() -> extractCristinInstitutionIdentifier(requestInfo)).orElse(fail -> null);

        return new PersonPatchFieldFilter(objectNode)
                   .filterOnInstNr(clientInstNr)
                   .getFiltered();
    }

    private void checkHasFields(ObjectNode cristinJson) throws BadRequestException {
        if (cristinJson.isEmpty()) {
            throw new BadRequestException(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD);
        }
    }

    private void checkIdentifiersMatch(String personId, String personIdFromCognito) throws BadRequestException {
        if (notEqual(personId, personIdFromCognito)) {
            throw new BadRequestException(ERROR_MESSAGE_IDENTIFIERS_DO_NOT_MATCH);
        }
    }

    private boolean notEqual(String personId, String personIdFromCognito) {
        return !Objects.equals(personId, personIdFromCognito);
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

    @Override
    protected void validateRequest(String input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateHasAccessRights(requestInfo);
    }

    private void validateHasAccessRights(RequestInfo requestInfo) throws ForbiddenException, UnauthorizedException {
        if (!AccessUtils.requesterIsUserAdministrator(requestInfo) && !clientCanUpdateOwnData(requestInfo)) {
            var personIdFromQuery = attempt(() -> getValidPersonId(requestInfo)).orElse(e -> PERSON_ID_NOT_FOUND);
            logFailedAuthorization(requestInfo, personIdFromQuery);
            throw new ForbiddenException();
        }
    }
}
